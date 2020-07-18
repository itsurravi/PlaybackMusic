package com.ravisharma.playbackmusic.fragments;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.activities.PlaylistActivity;
import com.ravisharma.playbackmusic.activities.RecentAddedActivity;
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.commoncode.ads.CustomAdSize;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.commoncode.alert.AlertClickListener;
import com.ravisharma.playbackmusic.commoncode.alert.PlaylistAlert;
import com.ravisharma.playbackmusic.prefrences.PrefManager;
import com.ravisharma.playbackmusic.prefrences.TinyDB;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import java.util.ArrayList;
import java.util.List;

import static com.ravisharma.playbackmusic.MainActivity.PLAYLIST;
import static com.ravisharma.playbackmusic.MainActivity.RECENT_ADDED;

public class PlaylistFragment extends Fragment implements PlaylistAdapter.OnPlaylistClicked
        ,PlaylistAdapter.OnPlaylistLongClicked, View.OnClickListener {

    private AdView adView;
    private FrameLayout adContainerView;

    private FastScrollRecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private PlaylistAdapter playlistAdapter;
    private List<String> playListArrayList;
    private Button btnAddNewPlaylist;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);

        playListArrayList = new ArrayList<>();

        btnAddNewPlaylist = v.findViewById(R.id.btnAddNewPlaylist);
        recyclerView = v.findViewById(R.id.playlist);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adContainerView = v.findViewById(R.id.banner_container_playlist);

        adView = new AdView(getContext());
        adView.setAdUnitId(getString(R.string.playlistFragId));
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        playlistAdapter = new PlaylistAdapter(getContext(), playListArrayList);
        recyclerView.setAdapter(playlistAdapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        playlistAdapter.setOnPlaylistClick(this);
        playlistAdapter.setOnPlaylistLongClick(this);

        btnAddNewPlaylist.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpArrayList();
    }

    private void setUpArrayList() {
        playListArrayList.clear();
        playListArrayList.add(getString(R.string.recentAdded));
        playListArrayList.add(getString(R.string.favTracks));

        PrefManager p = new PrefManager(getContext());
        ArrayList<String> list = p.getAllPlaylist();
        playListArrayList.addAll(list);

        playlistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onPlaylistClick(int position) {
        if (position == 0) {
            Intent i = new Intent(getContext(), RecentAddedActivity.class);
            getActivity().startActivityForResult(i, RECENT_ADDED);
        } else if (position >= 1) {
            Intent i = new Intent(getContext(), PlaylistActivity.class);
            i.putExtra("playlistName", playListArrayList.get(position));
            getActivity().startActivityForResult(i, PLAYLIST);
        }
    }

    @Override
    public void onClick(View view) {
        AlertClickListener listener = new AlertClickListener() {
            @Override
            public void OnOkClicked(String playlistName) {
                setUpArrayList();
            }
        };

        PlaylistAlert alert = new PlaylistAlert(getContext(), listener);
        alert.showCreateListAlert();
    }

    @Override
    public void onPlaylistLongClick(final int position) {
        if(position==0 || position==1){
            return;
        }

        String[] items = getResources().getStringArray(R.array.longPressItemsPlaylist);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, items);

        View v = LayoutInflater.from(getContext()).inflate(R.layout.alert_playlist, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        tv.setText(playListArrayList.get(position));
        lv.setAdapter(ad);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setView(v);

        final AlertDialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        alertDialog.show();
        final TinyDB tinydb = new TinyDB(getContext());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                switch (i){
                    case 0:
                        ArrayList<Song> songList = tinydb.getListObject(playListArrayList.get(position), Song.class);
                        MainActivity.getInstance().OnFragmentItemClick(0, songList, false);
                        break;
                    case 1:
                        tinydb.removeListObject(playListArrayList.get(position));
                        PrefManager p = new PrefManager(getContext());
                        p.deletePlaylist(playListArrayList.get(position));
                        playListArrayList.remove(position);
                        playlistAdapter.notifyDataSetChanged();
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }
}
