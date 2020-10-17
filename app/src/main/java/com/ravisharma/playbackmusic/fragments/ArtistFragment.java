package com.ravisharma.playbackmusic.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ravisharma.playbackmusic.activities.ArtistSongsActivity;
import com.ravisharma.playbackmusic.adapters.ArtistAdapter;
import com.ravisharma.playbackmusic.provider.SongsProvider;
import com.ravisharma.playbackmusic.model.Artist;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.ravisharma.playbackmusic.MainActivity.ARTIST_SONGS;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment implements ArtistAdapter.OnArtistClicked {

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
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        artistList = new ArrayList<Artist>();

        recyclerView = v.findViewById(R.id.artist_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        SongsProvider.Companion.getArtistList().observe(this, new Observer<ArrayList<Artist>>() {
            @Override
            public void onChanged(ArrayList<Artist> artists) {
                if (artists.size() > 0) {
                    artistList.clear();
                    artistList.addAll(artists);

                    Collections.sort(artistList, new Comparator<Artist>() {
                        public int compare(Artist a, Artist b) {
                            return a.getArtistName().compareTo(b.getArtistName());
                        }
                    });

                    adapter.notifyDataSetChanged();
                }
            }
        });


        adapter = new ArtistAdapter(getContext(), artistList);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
    }

    @Override
    public void onArtistClick(int position) {
        Intent i = new Intent(getContext(), ArtistSongsActivity.class);
        i.putExtra("artistId", String.valueOf(artistList.get(position).getArtistId()));
        getActivity().startActivityForResult(i, ARTIST_SONGS);
    }
}