package com.ravisharma.playbackmusic.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.Adapters.SongAdapter;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Prefrences.TinyDB;
import com.ravisharma.playbackmusic.Provider.Provider;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class PlaylistActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;
    private TextView txtPlaylistName;
    private ArrayList<Song> songList;
    private FastScrollRecyclerView recyclerView;
    private SongAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    TinyDB tinydb;
    String playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_playlist);
        songList = new ArrayList<>();
        tinydb = new TinyDB(getApplicationContext());

        playlistName = getIntent().getStringExtra("playlistName");
        txtPlaylistName = findViewById(R.id.txtPlaylistName);
        txtPlaylistName.setText(playlistName);

        fetchPlayList();

        initRecyclerView();

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adView = findViewById(R.id.banner_container_fav);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.song_list);
        recyclerView.setHasFixedSize(true);

        adapter = new SongAdapter(songList, this);
        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
        adapter.setOnLongClick(this);
    }

    private void fetchPlayList() {
        songList = tinydb.getListObject(playlistName, Song.class);
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
    public void onItemLongClick(final int mposition) {
        String[] items = getResources().getStringArray(R.array.longPressItemsRemove);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

        View v = LayoutInflater.from(this).inflate(R.layout.alert_list, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        tv.setText(songList.get(mposition).getTitle());
        lv.setAdapter(ad);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setView(v);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        alertDialog.show();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        PlaylistActivity.this.onItemClick(mposition);
                        break;
                    case 1:
                        // Delete Song Code
                        ArrayList<Song> list = tinydb.getListObject(playlistName, Song.class);
                        list.remove(songList.get(mposition));
                        tinydb.putListObject(playlistName, list);
                        if(playlistName.equals(getString(R.string.favTracks))){
                            MainActivity.getInstance().checkInFav(songList.get(mposition));
                        }
                        songList.remove(mposition);
                        adapter.notifyItemRemoved(mposition);
                        break;
                    case 2:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mposition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        startActivity(Intent.createChooser(intent, "Share Via"));
                        break;
                    case 3:
                        songDetails(mposition);
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void songDetails(int pos) {
        View v = LayoutInflater.from(this).inflate(R.layout.info, null);
        TextView title, artist, album, composer, duration, location;
        title = v.findViewById(R.id.info_title);
        artist = v.findViewById(R.id.info_artist);
        album = v.findViewById(R.id.info_album);
        composer = v.findViewById(R.id.info_composer);
        duration = v.findViewById(R.id.info_duration);
        location = v.findViewById(R.id.info_location);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);

        title.setText(songList.get(pos).getTitle());
        artist.setText(songList.get(pos).getArtist());
        album.setText(songList.get(pos).getAlbum());
        composer.setText(songList.get(pos).getComposer());
        duration.setText((String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(pos).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(pos).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(pos).getDuration())))));
        location.setText(songList.get(pos).getData());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialog.show();
    }


    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}