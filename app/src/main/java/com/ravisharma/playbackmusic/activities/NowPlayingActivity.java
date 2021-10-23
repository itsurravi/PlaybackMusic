package com.ravisharma.playbackmusic.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.ravisharma.playbackmusic.adapters.NowPlayingAdapter;
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.prefrences.PrefManager;
import com.ravisharma.playbackmusic.provider.SongsProvider;
import com.ravisharma.playbackmusic.utils.StartDragListener;
import com.ravisharma.playbackmusic.utils.UtilsKt;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener;
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class NowPlayingActivity extends AppCompatActivity implements NowPlayingAdapter.OnItemClicked, StartDragListener {

    private FrameLayout adContainerView;
    private AdView adView;

    private RecyclerView.LayoutManager layoutManager;
    private ItemTouchHelper itemTouchHelper;

    private ImageView imgBack, songArt;
    private FastScrollRecyclerView recyclerView;
    private NowPlayingAdapter adapter;
    private TextView songTitle, songArtist, songDuration;

    private int curpos;
    private PlaylistRepository repository;

    private Song playingSong;
    private ArrayList<Song> playingList;
    private int deletePosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        imgBack = findViewById(R.id.imgBack);
        songArt = findViewById(R.id.songArt);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        songDuration = findViewById(R.id.songDuration);

        playingList = new ArrayList<>();

        repository = new PlaylistRepository(this);

        Bundle b = getIntent().getExtras();
        curpos = b.getInt("songPos");

        initRecyclerView();

        UtilsKt.getPlayingListData().observe(this, songs -> {
            Log.d("Playing", "NowPlaying Playlist");
            playingList = songs;
            adapter.setList(playingList);
            if (UtilsKt.getSwiped() || UtilsKt.getFileDelete()) {
                Log.d("Playing", "Swiped");
                if (playingList.size() > 0) {
                    UtilsKt.setPlayingList(playingList);
                    int position = playingList.indexOf(playingSong);
                    MainActivity.Companion.getInstance().getMusicSrv().setSong(position);
                } else {
                    if (SongsProvider.Companion.getSongListByName().getValue().size() == 0) {
                        Toast.makeText(NowPlayingActivity.this, "No Song Left in Storage", Toast.LENGTH_SHORT).show();
                        PrefManager manage = new PrefManager(NowPlayingActivity.this);
                        manage.storeInfo(getString(R.string.Songs), false);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                System.exit(0);
                            }
                        }, 1000);
                    }
                    UtilsKt.setPlayingList(playingList);
                    MainActivity.Companion.getInstance().getMusicSrv().setSong(0);
                    MainActivity.Companion.getInstance().getMusicSrv().playSong();
                }
                UtilsKt.setSwiped(false);
                UtilsKt.setFileDelete(false);
            }
            if (UtilsKt.getMoved()) {
                UtilsKt.setPlayingList(playingList);
                int position = playingList.indexOf(playingSong);
                MainActivity.Companion.getInstance().getMusicSrv().setSong(position);
                UtilsKt.setMoved(false);
            }
        });

        UtilsKt.getCurPlayingSong().observe(this, new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                Log.d("Playing", "Chnaged");
                playingSong = song;
                songTitle.setText(playingSong.getTitle());
                songArtist.setText(playingSong.getArtist());
                songDuration.setText((String.format("%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(playingSong.getDuration()),
                        TimeUnit.MILLISECONDS.toSeconds(playingSong.getDuration()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(playingSong.getDuration())))));

                /*Song art code here*/
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.logo);
                requestOptions.error(R.drawable.logo);

                Glide.with(NowPlayingActivity.this)
                        .setDefaultRequestOptions(requestOptions)
                        .load(Uri.parse(playingSong.getArt()))
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .into(songArt);

            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adContainerView = findViewById(R.id.banner_container_nowPlaying);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.nowPlayingActId));
        adContainerView.addView(adView);
        loadBanner();
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.song_list);
        recyclerView.setHasFixedSize(true);

        adapter = new NowPlayingAdapter(this, this);
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        layoutManager.scrollToPosition(curpos);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);

        itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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
        deletePosition = position;
        new LongClickItems(this, position, playingList, "NowPlaying");
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
        AdSize adSize = AdSize.BANNER;
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
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
        ArrayList<Song> list = playingList;
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

            Collections.swap(playingList, fromPosition, toPosition);
            UtilsKt.setMoved(true);
            adapter.setList(playingList);

            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            if (playingList.get(position).equals(playingSong)) {
                if (playingList.size() > 0) {
                    MainActivity.Companion.getInstance().playNext();
                }
            }
            playingList.remove(position);
            adapter.setList(playingList);
            UtilsKt.setSwiped(true);
            updatePlayingList();
        }

        @Override
        public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if (!MainActivity.Companion.getInstance().played) {
                return 0;
            }
            return super.getSwipeDirs(recyclerView, viewHolder);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (UtilsKt.getMoved()) {
                updatePlayingList();
            }
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    };


    private void updatePlayingList() {
        UtilsKt.setPlayingList(playingList);
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    public void updateList(int mposition) {
        Song song = playingList.get(mposition);
        playingList.remove(mposition);

        if (song.equals(playingSong) && playingList.size() > 1) {
            MainActivity.Companion.getInstance().playNext();
        }

        UtilsKt.setFileDelete(true);
        UtilsKt.setPlayingList(playingList);
    }
}