package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ravisharma.playbackmusic.adapters.PlaylistAdapter;
import com.ravisharma.playbackmusic.database.PlaylistRepository;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener;
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert;
import com.ravisharma.playbackmusic.prefrences.PrefManager;
import com.ravisharma.playbackmusic.prefrences.TinyDB;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class AddToPlaylistActivity extends AppCompatActivity implements PlaylistAdapter.OnPlaylistClicked
        , PlaylistAdapter.OnPlaylistLongClicked {

    private Button btnAddNewPlaylist;
    private FastScrollRecyclerView recyclerView;
    private PlaylistAdapter playlistAdapter;

    private Song song;
    private ArrayList<String> list;
//    private TinyDB tinydb;
    private PlaylistRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_playlist);

        ImageView imgBack = findViewById(R.id.imgBack);

        song = getIntent().getParcelableExtra("Song");

        recyclerView = findViewById(R.id.playlistRecycler);

        btnAddNewPlaylist = findViewById(R.id.btnAddNewPlaylist);

        repository = new PlaylistRepository(this);
//        tinydb = new TinyDB(getApplicationContext());

        list = new ArrayList<>();

        initRecyclerView();

        setUpArrayList();

        playlistAdapter.setOnPlaylistClick(this);
        playlistAdapter.setOnPlaylistLongClick(this);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnAddNewPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateListAlert();
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

        playlistAdapter = new PlaylistAdapter(this, list);
        recyclerView.setAdapter(playlistAdapter);
    }

    private void addToPlaylist(String playListName) {
        /*ArrayList<Song> list = tinydb.getListObject(playListName, Song.class);

        if (list.contains(song)) {
            Toast.makeText(this, "Already Present", Toast.LENGTH_SHORT).show();
        } else {
            list.add(song);
            tinydb.putListObject(playListName, list);
            Toast.makeText(this, "Song Added To Playlist", Toast.LENGTH_SHORT).show();
        }*/
        long exist = repository.isSongExist(playListName, song.getId());
        if(exist>0){
            Toast.makeText(this, "Already Present", Toast.LENGTH_SHORT).show();
        }
        else{
            Playlist p = new Playlist(0, playListName, song);
            repository.addSong(p);
            Toast.makeText(this, "Song Added To Playlist", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPlaylistClick(int position) {
        addToPlaylist(list.get(position));
        finish();
    }

    @Override
    public void onPlaylistLongClick(int position) {

    }

    private void showCreateListAlert() {
        AlertClickListener listener = new AlertClickListener() {
            @Override
            public void OnOkClicked(String playlistName) {
                setUpArrayList();
            }
        };

        PlaylistAlert alert = new PlaylistAlert(this, listener);
        alert.showCreateListAlert();
    }

    private void setUpArrayList() {
        if (list.size() > 0) {
            list.clear();
        }
        PrefManager p = new PrefManager(this);
        ArrayList<String> lis = p.getAllPlaylist();
        list.addAll(lis);
        playlistAdapter.notifyDataSetChanged();
    }

}