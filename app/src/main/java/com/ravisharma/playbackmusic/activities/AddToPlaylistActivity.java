package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ravisharma.playbackmusic.activities.viewmodel.AddToPlaylistViewModel;
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


    private AddToPlaylistViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_playlist);

        ImageView imgBack = findViewById(R.id.imgBack);

        song = getIntent().getParcelableExtra("Song");

        recyclerView = findViewById(R.id.playlistRecycler);

        btnAddNewPlaylist = findViewById(R.id.btnAddNewPlaylist);

        list = new ArrayList<>();

        viewModel = new ViewModelProvider(this).get(AddToPlaylistViewModel.class);

        initRecyclerView();

        setUpArrayList();

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

        playlistAdapter.setOnPlaylistClick(this);
        playlistAdapter.setOnPlaylistLongClick(this);
    }

    private void setUpArrayList() {
        viewModel.getAllPlaylists(this).observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                list.clear();
                list.addAll(strings);
                playlistAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPlaylistClick(int position) {
        viewModel.addToPlaylist(this, list.get(position), song);
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

}