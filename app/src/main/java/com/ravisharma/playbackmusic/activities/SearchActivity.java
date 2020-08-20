package com.ravisharma.playbackmusic.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;
    private FrameLayout adContainerView;

    ImageView tvSearch;
    EditText edSearch;
    ImageView imgBack;
    FastScrollRecyclerView recyclerView;
    SongAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    ArrayList<Song> songList;
    String searchName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.SearchTheme);
        setContentView(R.layout.activity_search);

        tvSearch = findViewById(R.id.tvSearch);
        edSearch = findViewById(R.id.edSearch);
        imgBack = findViewById(R.id.imgBack);
        recyclerView = findViewById(R.id.song_list);

        songList = new ArrayList<>();

        initRecyclerView();

        initListeners();

        edSearch.requestFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.showSoftInput(edSearch, 0);
        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adContainerView = findViewById(R.id.banner_container_search);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.searchActId));
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

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void initListeners() {
        edSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String name = edSearch.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(SearchActivity.this, getString(R.string.notValid), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    performSearch(name);
                    return true;
                }
                return false;
            }
        });

        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = edSearch.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(SearchActivity.this, getString(R.string.notValid), Toast.LENGTH_SHORT).show();
                    return;
                }
                performSearch(name);
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void performSearch(String name) {
        searchName = name;
        edSearch.clearFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edSearch.getWindowToken(), 0);
        new MySearch().execute();
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
        adapter.notifyDataSetChanged();
    }

    class MySearch extends AsyncTask<Void, Void, ArrayList<Song>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Song> doInBackground(Void... voids) {
            songList.clear();

            ContentResolver musicResolver = SearchActivity.this.getContentResolver();
            Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            Cursor musicCursor = musicResolver.query(musicUri, null,
                    MediaStore.Audio.Media.TITLE + " LIKE \"%" + searchName + "%\" OR " +
                            MediaStore.Audio.Media.DISPLAY_NAME + " LIKE \"%" + searchName + "%\" OR " +
                            MediaStore.Audio.Media.ALBUM + " LIKE \"%" + searchName + "%\" OR " +
                            MediaStore.Audio.Media.ARTIST + " LIKE \"%" + searchName + "%\"",
                    null,
                    MediaStore.Audio.Media.TITLE + " ASC");

            if (musicCursor != null && musicCursor.moveToFirst()) {
                //get columns
                int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
                int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
                int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                int composerColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
                int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int dateModifyColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);


                //add songs to list
                do {
                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    String thisAlbum = musicCursor.getString(albumColumn);
                    String thisComposer = musicCursor.getString(composerColumn);
                    String thisPath = musicCursor.getString(pathColumn);
                    String thisDateModify = musicCursor.getString(dateModifyColumn);
                    long thisDuration = musicCursor.getLong(durationColumn);
                    long thisAlbumAid = musicCursor.getLong(albumIdColumn);
                    final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);

                    songList.add(new Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, String.valueOf(albumArt), thisDuration, thisAlbum, thisComposer));
                }
                while (musicCursor.moveToNext());
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Song> songs) {
            super.onPostExecute(songs);
            adapter = new SongAdapter(songList, SearchActivity.this);
            recyclerView.setAdapter(adapter);

            adapter.setOnClick(SearchActivity.this);
            adapter.setOnLongClick(SearchActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
}