package com.ravisharma.playbackmusic.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
import com.ravisharma.playbackmusic.activities.viewmodel.SearchViewModel;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.ravisharma.playbackmusic.utils.UtilsKt;
import com.ravisharma.playbackmusic.utils.longclick.LongClickItems;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;
    private FrameLayout adContainerView;

    private ImageView tvSearch;
    private EditText edSearch;
    private ImageView imgBack;
    private FastScrollRecyclerView recyclerView;
    private SongAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Song> songList;

    private SearchViewModel viewModel;

    private int mposition;

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

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        initRecyclerView();

        initListeners();

        edSearch.requestFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.showSoftInput(edSearch, 0);

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

        adapter = new SongAdapter(songList, SearchActivity.this);
        recyclerView.setAdapter(adapter);

        adapter.setOnClick(SearchActivity.this);
        adapter.setOnLongClick(SearchActivity.this);

        viewModel.getSearchList().observe(this, new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                if (songs.size() > 0) {
                    songList.clear();
                    songList.addAll(songs);
                    adapter.notifyDataSetChanged();
                }
            }
        });
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
        if(name.length()<3){
            Toast.makeText(this, "Search Length Not Valid", Toast.LENGTH_SHORT).show();
            return;
        }
        edSearch.clearFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(edSearch.getWindowToken(), 0);
        viewModel.search(name, getContentResolver());
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
        this.mposition=mposition;
        new LongClickItems(this, mposition, songList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(UtilsKt.getDeleteUri()!=null) {
                getContentResolver().delete(UtilsKt.getDeleteUri(), null, null);
                songList.remove(mposition);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
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