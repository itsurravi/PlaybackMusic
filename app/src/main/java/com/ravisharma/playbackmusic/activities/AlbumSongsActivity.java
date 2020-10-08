package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.activities.viewmodel.AlbumSongViewModel;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class AlbumSongsActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private FrameLayout adContainerView;
    private AdView adView;

    private ImageView albumArt, imgBack;
    private TextView albumTitle, albumSong;
    private FastScrollRecyclerView recyclerView;
    private SongAdapter ad;

    private ArrayList<Song> songList;

    private AlbumSongViewModel viewModel;

    private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        viewModel = new ViewModelProvider(this).get(AlbumSongViewModel.class);

        String albumId = getIntent().getExtras().getString("albumId");

        if (albumId == null) {
            finish();
            return;
        }
        this.albumId = albumId;

        initRecyclerView();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adContainerView = findViewById(R.id.banner_container_albumActivity);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.AlbumSongsActId));
        adContainerView.addView(adView);
        loadBanner();
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        ad = new SongAdapter(songList, this);
        recyclerView.setAdapter(ad);
        ad.setOnClick(this);
        ad.setOnLongClick(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        viewModel.getAlbumSongs(albumId, getContentResolver()).observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                songList.clear();
                songList.addAll(songs);

                if (songList.size() > 0) {
                    /*Song art code here*/
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.placeholder(R.drawable.logo);
                    requestOptions.error(R.drawable.logo);

                    Glide.with(AlbumSongsActivity.this)
                            .setDefaultRequestOptions(requestOptions)
                            .load(Uri.parse(songList.get(0).getArt()))
                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                            .into(albumArt);

                    albumTitle.setText(songList.get(0).getAlbum());

                    int size = songList.size();
                    String noOfSongs = getResources().getQuantityString(R.plurals.numberOfSongs, size, size);

                    albumSong.setText(noOfSongs);
                    ad.notifyDataSetChanged();
                }
            }
        });
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
            ad.notifyDataSetChanged();
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

    public void onItemClick(ArrayList<Song> list) {
        Intent i = new Intent();
        i.putExtra("position", 0);
        i.putExtra("songList", list);
        setResult(RESULT_OK, i);
        finish();
    }
}
