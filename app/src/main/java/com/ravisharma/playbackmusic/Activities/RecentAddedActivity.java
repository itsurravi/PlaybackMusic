package com.ravisharma.playbackmusic.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.Adapters.SongAdapter;
import com.ravisharma.playbackmusic.LongClickItems;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Provider.Provider;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class RecentAddedActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;
    private FrameLayout adContainerView;

    ImageView imgBack;
    FastScrollRecyclerView recyclerView;
    private SongAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Song> recentSongList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_added);


        imgBack = findViewById(R.id.imgBack);
        recyclerView = findViewById(R.id.recent_song_list);

        recentSongList = MainActivity.provider.getSongListByDate();

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new SongAdapter(recentSongList, this);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(),
                        DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
        adapter.setOnLongClick(this);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adContainerView = findViewById(R.id.banner_container_recentActivity);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.recentSongsActId));
        adContainerView.addView(adView);
        loadBanner();
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

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent();
        i.putExtra("position", position);
        i.putExtra("songList", recentSongList);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onItemLongClick(final int mposition) {
        new LongClickItems(this, mposition, recentSongList);
    }

    public void updateList(int mposition) {
        recentSongList.remove(mposition);
        if (recentSongList.size() > 0) {
            adapter.notifyDataSetChanged();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}