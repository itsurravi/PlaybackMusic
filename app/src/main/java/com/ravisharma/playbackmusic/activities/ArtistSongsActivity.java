package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.activities.viewmodel.ArtistSongViewModel;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class ArtistSongsActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;
    private FrameLayout adContainerView;

    private ImageView imgBack;
    private TextView artistName;
    private FastScrollRecyclerView recyclerView;
    private SongAdapter ad;

    private ArrayList<Song> songList;

    private ArtistSongViewModel viewModel;

    private String artistId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_songs);

        songList = new ArrayList<Song>();

        imgBack = findViewById(R.id.imgBack);
        artistName = findViewById(R.id.artistName);
        recyclerView = findViewById(R.id.song_list);

        viewModel = new ViewModelProvider(this).get(ArtistSongViewModel.class);

        String artistId = getIntent().getExtras().getString("artistId");

        if (artistId == null) {
            finish();
            return;
        }

        this.artistId = artistId;

        initRecyclerView();

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        adContainerView = findViewById(R.id.banner_container_artistActivity);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.artistSongsActId));
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

        viewModel.getArtistSongs(artistId, getContentResolver()).observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                songList.clear();
                songList.addAll(songs);
                ad.notifyDataSetChanged();
                artistName.setText(songList.get(0).getArtist());

            }
        });
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
