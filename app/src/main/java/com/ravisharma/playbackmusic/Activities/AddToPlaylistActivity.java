package com.ravisharma.playbackmusic.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.ravisharma.playbackmusic.Adapters.PlaylistAdapter;
import com.ravisharma.playbackmusic.Adapters.SongAdapter;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Prefrences.PrefManager;
import com.ravisharma.playbackmusic.Prefrences.TinyDB;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AddToPlaylistActivity extends AppCompatActivity implements PlaylistAdapter.OnPlaylistClicked
        , PlaylistAdapter.OnPlaylistLongClicked {

    private FastScrollRecyclerView recyclerView;

    private Song song;
    private ArrayList<String> list;
    private TinyDB tinydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_playlist);

        ImageView imgBack = findViewById(R.id.imgBack);
        List<String> playListArrayList = new ArrayList<>();

        song = getIntent().getParcelableExtra("Song");

        recyclerView = findViewById(R.id.playlistRecycler);

        tinydb = new TinyDB(getApplicationContext());

        initRecyclerView();

        PrefManager p = new PrefManager(this);
        list = p.getAllPlaylist();
        playListArrayList.addAll(list);

        PlaylistAdapter playlistAdapter = new PlaylistAdapter(this, playListArrayList);
        recyclerView.setAdapter(playlistAdapter);

        playlistAdapter.setOnPlaylistClick(this);
        playlistAdapter.setOnPlaylistLongClick(this);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void addToFavPlaylist(String playListName) {
        ArrayList<Song> list = tinydb.getListObject(playListName, Song.class);

        if (list.contains(song)) {
            Toast.makeText(this, "Already Present", Toast.LENGTH_SHORT).show();
        } else {
            list.add(song);
            tinydb.putListObject(playListName, list);
            Toast.makeText(this, "Song Added To Playlist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPlaylistClick(int position) {
        addToFavPlaylist(list.get(position));
        finish();
    }

    @Override
    public void onPlaylistLongClick(int position) {

    }
}