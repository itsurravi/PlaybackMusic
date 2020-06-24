package com.ravisharma.playbackmusic.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ravisharma.playbackmusic.Adapters.PlaylistAdapter;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Prefrences.PrefManager;
import com.ravisharma.playbackmusic.Prefrences.TinyDB;
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
    private TinyDB tinydb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_playlist);

        ImageView imgBack = findViewById(R.id.imgBack);

        song = getIntent().getParcelableExtra("Song");

        recyclerView = findViewById(R.id.playlistRecycler);

        btnAddNewPlaylist = findViewById(R.id.btnAddNewPlaylist);

        tinydb = new TinyDB(getApplicationContext());

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
                showCreateListAler();
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
        addToPlaylist(list.get(position));
        finish();
    }

    @Override
    public void onPlaylistLongClick(int position) {

    }

    private void showCreateListAler() {
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
                    PrefManager p = new PrefManager(AddToPlaylistActivity.this);
                    p.createNewPlaylist(playlistName);
                    setUpArrayList();
                }
                edPlayListName.setText("");
                alertDialog.dismiss();
            }
        });
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