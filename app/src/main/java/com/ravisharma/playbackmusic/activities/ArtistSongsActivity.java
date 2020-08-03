package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
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
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.ravisharma.playbackmusic.commoncode.longclick.LongClickItems;
import com.ravisharma.playbackmusic.commoncode.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.provider.Provider;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class ArtistSongsActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;
    private FrameLayout adContainerView;

    ImageView imgBack;
    TextView artistName;
    FastScrollRecyclerView recyclerView;

    private ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_songs);

        songList = new ArrayList<Song>();

        imgBack = findViewById(R.id.imgBack);
        artistName = findViewById(R.id.artistName);
        recyclerView = findViewById(R.id.song_list);

        String artistId = getIntent().getExtras().getString("artistId");

        if (artistId == null) {
            finish();
            return;
        }

        Provider p = new Provider(this);
        songList.addAll(p.getSongListByArtist(artistId));

        artistName.setText(songList.get(0).getArtist());

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

        adContainerView = findViewById(R.id.banner_container_artistActivity);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.artistSongsActId));
        adContainerView.addView(adView);
        loadBanner();
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
