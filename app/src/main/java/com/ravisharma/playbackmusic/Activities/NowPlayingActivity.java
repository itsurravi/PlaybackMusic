package com.ravisharma.playbackmusic.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.ravisharma.playbackmusic.Adapters.NowPlayingAdapter;
import com.ravisharma.playbackmusic.LongClickItems;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Prefrences.PrefManager;
import com.ravisharma.playbackmusic.Prefrences.TinyDB;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class NowPlayingActivity extends AppCompatActivity implements NowPlayingAdapter.OnItemClicked {

    private final String TAG = "NowPlayingAct";

    private FrameLayout adContainerView;
    private AdView adView;

    ImageView imgBack;
    FastScrollRecyclerView recyclerView;
    NowPlayingAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    int curpos;
    TinyDB tinydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        imgBack = findViewById(R.id.imgBack);

        Bundle b = getIntent().getExtras();

        curpos = b.getInt("songPos");

        tinydb = new TinyDB(getApplicationContext());

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
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    public void updateList(int mposition) {
        if (MainActivity.getInstance().songList.size() > 0) {
            adapter.notifyDataSetChanged();
        } else {
            finish();
        }
    }

    public void showCreateListAlert(final View view) {
        View v = LayoutInflater.from(this).inflate(R.layout.alert_create_playlist, null);

        final EditText edPlayListName = v.findViewById(R.id.edPlaylistName);
        TextView tvCancel = v.findViewById(R.id.tvCancel);
        TextView tvOk = v.findViewById(R.id.tvOk);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(v);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        alertDialog.show();

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String playlistName = edPlayListName.getText().toString().trim();
                if (playlistName.length() > 0) {
                    playlistName = playlistName.substring(0, 1).toUpperCase().concat(playlistName.substring(1));
                    PrefManager p = new PrefManager(NowPlayingActivity.this);
                    p.createNewPlaylist(playlistName);
                    addToPlaylist(playlistName);
                    Snackbar.make(view, "Playlist Saved", Snackbar.LENGTH_SHORT).show();
                }
                edPlayListName.setText("");
                alertDialog.dismiss();
            }
        });
    }

    private void addToPlaylist(String playListName) {
        ArrayList<Song> list = MainActivity.getInstance().songList;
        tinydb.putListObject(playListName, list);
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
            if(!MainActivity.getInstance().played){
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