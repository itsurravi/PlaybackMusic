package com.ravisharma.playbackmusic.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.activities.ArtistSongsActivity;
import com.ravisharma.playbackmusic.adapters.ArtistAdapter;
import com.ravisharma.playbackmusic.DataUpdateListener;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.commoncode.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Artist;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static com.ravisharma.playbackmusic.MainActivity.ARTIST_SONGS;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment implements ArtistAdapter.OnArtistClicked,
        DataUpdateListener {
    private AdView adView;
    private FrameLayout adContainerView;

    FastScrollRecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Artist> artistList;

    ArtistAdapter adapter;

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        artistList = MainActivity.provider.getArtistList();
        ((MainActivity) Objects.requireNonNull(getActivity())).registerDataUpdateListener(this);

        recyclerView = v.findViewById(R.id.artist_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Collections.sort(artistList, new Comparator<Artist>() {
            public int compare(Artist a, Artist b) {
                return a.getArtistName().compareTo(b.getArtistName());
            }
        });

        adapter = new ArtistAdapter(getContext(), artistList);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adContainerView = v.findViewById(R.id.banner_container_artist);

        adView = new AdView(getContext());
        adView.setAdUnitId(getString(R.string.artistFragId));
        adContainerView.addView(adView);
        loadBanner();

        return v;
    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = CustomAdSize.getAdSize(getActivity());
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    @Override
    public void onArtistClick(int position) {
        Intent i = new Intent(getContext(), ArtistSongsActivity.class);
        i.putExtra("artistId", String.valueOf(artistList.get(position).getArtistId()));
        getActivity().startActivityForResult(i, ARTIST_SONGS);
    }

    @Override
    public void onDataUpdate() {
        artistList = MainActivity.provider.getArtistList();
        adapter.setList(artistList);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        ((MainActivity) getActivity()).unregisterDataUpdateListener(this);
        super.onDestroy();
    }
}