package com.ravisharma.playbackmusic.fragments;

import android.content.Context;
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

import com.ravisharma.playbackmusic.adapters.AlbumAdapter;
import com.ravisharma.playbackmusic.activities.AlbumSongsActivity;
import com.ravisharma.playbackmusic.provider.SongsProvider;
import com.ravisharma.playbackmusic.model.Album;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.ravisharma.playbackmusic.MainActivity.ALBUM_SONGS;

public class AlbumsFragment extends Fragment implements AlbumAdapter.OnAlbumClicked {

    FastScrollRecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Album> albumsList;

    AlbumAdapter adapter;

    public AlbumsFragment() {
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
        View v = inflater.inflate(R.layout.fragment_albums, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        albumsList = new ArrayList<>();

        recyclerView = v.findViewById(R.id.album_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        SongsProvider.Companion.getAlbumList().observe(this, new Observer<ArrayList<Album>>() {
            @Override
            public void onChanged(ArrayList<Album> albums) {
                if (albums.size() > 0) {
                    albumsList.clear();
                    albumsList.addAll(albums);

                    Collections.sort(albumsList, new Comparator<Album>() {
                        public int compare(Album a, Album b) {
                            return a.getAlbumName().compareTo(b.getAlbumName());
                        }
                    });
                    adapter.notifyDataSetChanged();
                }
            }
        });

        adapter = new AlbumAdapter(getContext(), albumsList);
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
    }

    @Override
    public void onAlbumClick(int position) {
        Intent i = new Intent(getContext(), AlbumSongsActivity.class);
        i.putExtra("albumId", String.valueOf(albumsList.get(position).getAlbumId()));
        getActivity().startActivityForResult(i, ALBUM_SONGS);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
    }
}
