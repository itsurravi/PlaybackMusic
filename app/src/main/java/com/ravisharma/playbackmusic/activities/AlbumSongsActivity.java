package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.provider.Provider;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class AlbumSongsActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private FrameLayout adContainerView;
    private AdView adView;

    ImageView albumArt, imgBack;
    TextView albumTitle, albumSong;
    FastScrollRecyclerView recyclerView;

    private ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // In Activity's onCreate() for instance
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_album_songs);

        songList = new ArrayList<Song>();

        albumArt = findViewById(R.id.albumArt);
        imgBack = findViewById(R.id.imgBack);
        albumTitle = findViewById(R.id.albumTitle);
        albumSong = findViewById(R.id.albumSong);
        recyclerView = findViewById(R.id.song_list);

        String albumId = getIntent().getExtras().getString("albumId");

        if (albumId == null) {
            finish();
            return;
        }

        Provider p = new Provider(this);
        songList.addAll(p.getSongList(albumId));

        if (songList.size() > 0) {
            /*Song art code here*/
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.logo);
            requestOptions.error(R.drawable.logo);

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList.get(0).getArt()))
                    .into(albumArt);

            albumTitle.setText(songList.get(0).getAlbum());
        } else {
            finish();
            Toast.makeText(this, "No Song Found", Toast.LENGTH_SHORT).show();
        }

        albumSong.setText(songList.size() + " Song(s)");
        recyclerView.setHasFixedSize(true);

        SongAdapter ad = new SongAdapter(songList, this);
        recyclerView.setAdapter(ad);
        ad.setOnClick(this);
        ad.setOnLongClick(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adContainerView = findViewById(R.id.banner_container_albumActivity);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.AlbumSongsActId));
        adContainerView.addView(adView);
        loadBanner();
    }

    public void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = CustomAdSize.getAdSize(this);
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    @Override
    public void onItemClick(int position) {
        Intent i = new Intent();
        i.putExtra("position", position);
        i.putExtra("songList", songList);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onItemLongClick(final int mposition) {
        new LongClickItems(this, mposition, songList);
    }

    public void updateList(int mposition) {
        songList.remove(mposition);
        if (songList.size() > 0) {
            recyclerView.getAdapter().notifyDataSetChanged();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        songList.clear();
        super.onDestroy();
    }
}
