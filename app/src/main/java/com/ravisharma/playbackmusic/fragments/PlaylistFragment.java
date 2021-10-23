package com.ravisharma.playbackmusic.fragments;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.adapters.PlaylistAdapter;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.fragments.viewmodels.PlaylistFragmentViewModel;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.utils.alert.AlertClickListener;
import com.ravisharma.playbackmusic.utils.alert.PlaylistAlert;
import com.ravisharma.playbackmusic.prefrences.PrefManager;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment implements PlaylistAdapter.OnPlaylistClicked
        , PlaylistAdapter.OnPlaylistLongClicked, View.OnClickListener {

    private CardView cardRecentAdded, cardLastPlayed, cardMostPlayed;
    private FastScrollRecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private PlaylistAdapter playlistAdapter;
    private List<String> playListArrayList;
    private Button btnAddNewPlaylist;

    private PlaylistFragmentViewModel viewModel;

    private AdView adView;
    private FrameLayout adContainerView;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);
        playListArrayList = new ArrayList<>();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        btnAddNewPlaylist = v.findViewById(R.id.btnAddNewPlaylist);
        recyclerView = v.findViewById(R.id.playlist);
        cardRecentAdded = v.findViewById(R.id.card_recentAdded);
        cardLastPlayed = v.findViewById(R.id.card_lastPlayed);
        cardMostPlayed = v.findViewById(R.id.card_mostPlayed);

        viewModel = new ViewModelProvider(this).get(PlaylistFragmentViewModel.class);

        initRecyclerView();

        adContainerView = v.findViewById(R.id.banner_container_playlist);

        adView = new AdView(getContext());
        adView.setAdUnitId(getString(R.string.playlistFragId));
        adContainerView.addView(adView);
        loadBanner2();

        cardRecentAdded.setOnClickListener(this);
        cardLastPlayed.setOnClickListener(this);
        cardMostPlayed.setOnClickListener(this);
    }

    private void loadBanner2() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = AdSize.BANNER;
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpArrayList();
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void initRecyclerView() {
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL,
                false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        playlistAdapter = new PlaylistAdapter(getContext(), playListArrayList);
        recyclerView.setAdapter(playlistAdapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        playlistAdapter.setOnPlaylistClick(this);
        playlistAdapter.setOnPlaylistLongClick(this);

        btnAddNewPlaylist.setOnClickListener(this);
    }

    private void setUpArrayList() {
        viewModel.getAllPlaylists(getContext()).observe(this, new Observer<ArrayList<String>>() {
            @Override
            public void onChanged(ArrayList<String> strings) {
                playListArrayList.clear();
                playListArrayList.add(getString(R.string.favTracks));
                playListArrayList.addAll(strings);
                playlistAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPlaylistClick(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("argType", CategorySongFragmentKt.PLAYLIST);
        bundle.putString("actName", playListArrayList.get(position));

        openFragment(bundle);
    }

    private void openFragment(Bundle bundle) {
        CategorySongFragment fragment = new CategorySongFragment();
        fragment.setArguments(bundle);
        MainActivity.Companion.getInstance().hideHomePanel();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(View view) {
        if (btnAddNewPlaylist.equals(view)) {
            showCreateUpdatePlaylistDialog(true, null);
            return;
        }

        Bundle bundle = new Bundle();

        if (cardRecentAdded.equals(view)) {
            bundle.putString("argType", "Recent Added");
            bundle.putString("actName", "Recent Added");
        } else if (cardLastPlayed.equals(view)) {
            bundle.putString("argType", "Last Played");
            bundle.putString("actName", "Last Played");
        } else if (cardMostPlayed.equals(view)) {
            bundle.putString("argType", "Most Played");
            bundle.putString("actName", "Most Played");
        }

        openFragment(bundle);
    }

    @Override
    public void onPlaylistLongClick(final int position) {
        if (position == 0) {
            return;
        }

        String[] items = getResources().getStringArray(R.array.longPressItemsPlaylist);
        ArrayAdapter<String> ad = new ArrayAdapter<>(getContext(), R.layout.adapter_alert_list, items);

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

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                switch (i) {
                    case 0:
                        List<Playlist> list = viewModel.getPlaylist(getContext(), playListArrayList.get(position));
                        if (list != null && list.size() > 0) {
                            ArrayList<Song> songList = new ArrayList<>();
                            for (Playlist p : list) {
                                songList.add(p.getSong());
                            }
                            if (songList.size() > 0) {
                                MainActivity.Companion.getInstance().OnFragmentItemClick(0, songList, false);
                            }
                        } else {
                            Toast.makeText(getContext(), "Playlist is Empty", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 1:
                        showCreateUpdatePlaylistDialog(false, playListArrayList.get(position));
                        break;
                    case 2:
                        viewModel.removePlaylist(getContext(), playListArrayList.get(position));
                        PrefManager p = new PrefManager(getContext());
                        p.deletePlaylist(playListArrayList.get(position));
                        setUpArrayList();
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void showCreateUpdatePlaylistDialog(final boolean createList, final String oldPlaylistName) {
        AlertClickListener listener = new AlertClickListener() {
            @Override
            public void OnOkClicked(String newPlaylistName) {
                if (!createList) {
                    viewModel.renamePlaylist(getContext(), oldPlaylistName, newPlaylistName);
                }
                setUpArrayList();
            }
        };

        PlaylistAlert alert = new PlaylistAlert(getContext(), listener);
        if (createList) {
            alert.showCreateListAlert();
        } else {
            alert.showUpdateListAlert(oldPlaylistName);
        }
    }
}
