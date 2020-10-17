package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.activities.viewmodel.LastAndMostPlayedViewModel;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.ravisharma.playbackmusic.database.model.LastPlayed;
import com.ravisharma.playbackmusic.database.model.MostPlayed;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LastAndMostPlayed extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private FrameLayout adContainerView;
    private AdView adView;

    private ImageView albumArt;
    private TextView txtTitle1, txtTitle2;
    private FastScrollRecyclerView recyclerView;
    private SongAdapter adapter;
    private ArrayList<Song> songList;
    private ConstraintLayout noDataLayout;
    private RelativeLayout firstLayout;
    private LinearLayout secondLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In Activity's onCreate() for instance
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_last_and_most_played);

        txtTitle1 = findViewById(R.id.txtTitle_1);
        txtTitle2 = findViewById(R.id.txtTitle_2);
        albumArt = findViewById(R.id.albumArt);
        recyclerView = findViewById(R.id.song_list);
        noDataLayout = findViewById(R.id.noDataLayout);
        firstLayout = findViewById(R.id.firstLayout);
        secondLayout = findViewById(R.id.secondLayout);

        LastAndMostPlayedViewModel viewModel = new ViewModelProvider(this)
                .get(LastAndMostPlayedViewModel.class);

        String actName = getIntent().getExtras().getString("actName");

        initRecyclerView();

        String adId = getString(R.string.albumFragId);

        if (actName != null) {
            txtTitle1.setText(actName);
            txtTitle2.setText(actName);
            if (actName.equals("Last Played")) {
                adId = getString(R.string.albumFragId);
                viewModel.getLastPlayedSongsList(this).observe(this,
                        new Observer<List<LastPlayed>>() {
                            @Override
                            public void onChanged(List<LastPlayed> lastPlayedList) {
                                songList.clear();
                                for (LastPlayed played : lastPlayedList) {
                                    Song song = played.getSong();
                                    songList.add(song);
                                }
                                adapter.notifyDataSetChanged();

                                setUpLayout();
                            }
                        });
            } else {
                adId = getString(R.string.artistFragId);
                viewModel.getMostPlayedSongsList(this).observe(this,
                        new Observer<List<MostPlayed>>() {
                            @Override
                            public void onChanged(List<MostPlayed> mostPlayedList) {
                                songList.clear();
                                for (MostPlayed played : mostPlayedList) {
                                    Song song = played.getSong();
                                    songList.add(song);
                                }

                                adapter.notifyDataSetChanged();

                                setUpLayout();
                            }
                        });
            }
        }

        adContainerView = findViewById(R.id.banner_container_lastMostPlayed);

        adView = new AdView(this);
        adView.setAdUnitId(adId);
        adContainerView.addView(adView);
        loadBanner();
    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = AdSize.BANNER;
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private void setUpLayout() {
        if (songList.size() == 0) {
            noDataLayout.setVisibility(View.VISIBLE);
            secondLayout.setVisibility(View.VISIBLE);
            firstLayout.setVisibility(View.GONE);
        } else {
            noDataLayout.setVisibility(View.GONE);
            secondLayout.setVisibility(View.GONE);
            firstLayout.setVisibility(View.VISIBLE);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.logo);
            requestOptions.error(R.drawable.logo);

            Glide.with(LastAndMostPlayed.this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList.get(0).getArt()))
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .into(albumArt);
        }
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

    private void initRecyclerView() {
        songList = new ArrayList<>();
        adapter = new SongAdapter(songList, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
        adapter.setOnLongClick(this);
    }

    public void finishPage(View view) {
        finish();
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
    public void onItemLongClick(int position) {
        new LongClickItems(this, position, songList);
    }

    public void onItemClick(ArrayList<Song> list) {
        Intent i = new Intent();
        i.putExtra("position", 0);
        i.putExtra("songList", list);
        setResult(RESULT_OK, i);
        finish();
    }
}