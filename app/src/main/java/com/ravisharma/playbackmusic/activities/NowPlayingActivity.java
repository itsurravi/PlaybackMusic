package com.ravisharma.playbackmusic.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.ravisharma.playbackmusic.adapters.NowPlayingAdapter;
import com.ravisharma.playbackmusic.database.PlaylistRepository;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener;
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert;
import com.ravisharma.playbackmusic.prefrences.TinyDB;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity implements NowPlayingAdapter.OnItemClicked {

    private FrameLayout adContainerView;
    private AdView adView;

    private RecyclerView.LayoutManager layoutManager;
    ImageView imgBack, songArt;
    FastScrollRecyclerView recyclerView;
    NowPlayingAdapter adapter;
    TextView songTitle, songArtist, songDuration;

    int curpos;
    //    TinyDB tinydb;
    private PlaylistRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        imgBack = findViewById(R.id.imgBack);
        songArt = findViewById(R.id.songArt);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        songDuration = findViewById(R.id.songDuration);

        Bundle b = getIntent().getExtras();
        curpos = b.getInt("songPos");

        Song surrentSong = MainActivity.getInstance().songList.get(curpos);

        songTitle.setText(surrentSong.getTitle());
        songArtist.setText(surrentSong.getArtist());
        songDuration.setText((String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(surrentSong.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(surrentSong.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(surrentSong.getDuration())))));

        /*Song art code here*/
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.logo);
        requestOptions.error(R.drawable.logo);

        Glide.with(this)
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(surrentSong.getArt()))
                .into(songArt);

//        tinydb = new TinyDB(getApplicationContext());
        repository = new PlaylistRepository(this);

        recyclerView = findViewById(R.id.song_list);
        recyclerView.setHasFixedSize(true);

        adapter = new NowPlayingAdapter(MainActivity.getInstance().songList, this);
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.scrollToPosition(curpos);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adContainerView = findViewById(R.id.banner_container_nowPlaying);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.nowPlayingActId));
        adContainerView.addView(adView);
        loadBanner();
    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent();
        i.putExtra("position", position);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onOptionsClick(int position) {
        new LongClickItems(this, position, MainActivity.getInstance().songList, "NowPlaying");
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = CustomAdSize.getAdSize(this);
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    public void updateList(int mposition) {
        if (MainActivity.getInstance().songList.size() > 0) {
            adapter.notifyDataSetChanged();
        } else {
            finish();
        }
    }

    public void showCreateListAlert(final View view) {
        AlertClickListener listener = new AlertClickListener() {
            @Override
            public void OnOkClicked(String playlistName) {
                addToPlaylist(playlistName);
                Snackbar.make(view, "Playlist Saved", Snackbar.LENGTH_SHORT).show();
            }
        };

        PlaylistAlert alert = new PlaylistAlert(this, listener);
        alert.showCreateListAlert();
    }

    private void addToPlaylist(String playListName) {
        ArrayList<Song> list = MainActivity.getInstance().songList;
//        tinydb.putListObject(playListName, list);
        for (Song s : list) {
            Playlist p = new Playlist(0, playListName, s);
            repository.addSong(p);
        }
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.START | ItemTouchHelper.END | ItemTouchHelper.UP |
                    ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(MainActivity.getInstance().songList, fromPosition, toPosition);

            updatePlayingList();

            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (MainActivity.getInstance().songList.get(position).equals(MainActivity.getInstance().getPlayingSong())) {
                if (MainActivity.getInstance().songList.size() > 0) {
                    MainActivity.getInstance().playNext();
                }
            }
            MainActivity.getInstance().songList.remove(position);
            recyclerView.getAdapter().notifyDataSetChanged();
            if (MainActivity.getInstance().songList.size() == 0) {
                MainActivity.getInstance().songList = MainActivity.provider.getSongListByName();
                MainActivity.getInstance().songPosn = 0;
                MainActivity.getInstance().musicSrv.setList(MainActivity.getInstance().songList);
                MainActivity.getInstance().musicSrv.setSong(0);
            } else {
                updatePlayingList();
            }
        }

        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (!MainActivity.getInstance().played) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }
    };


    private void updatePlayingList() {
        Song playingSong = MainActivity.getInstance().getPlayingSong();
        int position = MainActivity.getInstance().songList.indexOf(playingSong);
        MainActivity.getInstance().songPosn = position;
        MainActivity.getInstance().musicSrv.setList(MainActivity.getInstance().songList);
        MainActivity.getInstance().musicSrv.setSong(position);
    }
}