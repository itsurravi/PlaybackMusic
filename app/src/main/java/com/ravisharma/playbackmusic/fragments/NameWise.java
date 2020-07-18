package com.ravisharma.playbackmusic.fragments;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.activities.AddToPlaylistActivity;
import com.ravisharma.playbackmusic.DataUpdateListener;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.commoncode.ads.CustomAdSize;
import com.ravisharma.playbackmusic.provider.Provider;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class NameWise extends Fragment implements SongAdapter.OnItemClicked,
        SongAdapter.OnItemLongClicked, DataUpdateListener {

    private AdView adView;
    private FrameLayout adContainerView;

    FastScrollRecyclerView recyclerView;
    SongAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Song> songList;
    LayoutInflater li;
    boolean command = false;
    /*private OnFragmentInteractionListener mListener;*/

    public NameWise() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_name_wise, container, false);
        if (songList.size() > 0) {
            songList.clear();
        }

        songList.addAll(MainActivity.provider.getSongListByName());
        ((MainActivity) Objects.requireNonNull(getActivity())).registerDataUpdateListener(this);

        li = inflater;
        recyclerView = v.findViewById(R.id.song_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        /*if(MainActivity.getInstance().lastSongId!=null && !MainActivity.getInstance().fromRecent){
            MainActivity.getInstance().setStartingList(songList);
        }*/
        adapter = new SongAdapter(songList, getContext());
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
        adapter.setOnLongClick(this);
        //Toast.makeText(getContext(), "name", Toast.LENGTH_SHORT).show();

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adContainerView = v.findViewById(R.id.banner_container_name);

        adView = new AdView(getContext());
        adView.setAdUnitId(getString(R.string.nameFragId));
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
    public void onItemClick(int position) {
        OnFragmentItemClicked onFragmentItemClicked = (OnFragmentItemClicked) getActivity();
        onFragmentItemClicked.OnFragmentItemClick(position, songList, false);
    }

    @Override
    public void onItemLongClick(final int mposition) {
        String[] items = getResources().getStringArray(R.array.longPressItems);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, items);

        View v = li.inflate(R.layout.alert_list, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        ImageView songArt = v.findViewById(R.id.songArt);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.logo);
        requestOptions.error(R.drawable.logo);

        Glide.with(v)
                .setDefaultRequestOptions(requestOptions)
                .load(songList.get(mposition).getArt())
                .into(songArt);

        tv.setText(songList.get(mposition).getTitle());
        lv.setAdapter(ad);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
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
                        OnFragmentItemClicked onFragmentItemClicked = (OnFragmentItemClicked) getActivity();
                        onFragmentItemClicked.OnFragmentItemClick(mposition, songList, false);
                        break;
                    case 1:
                        ((MainActivity) getActivity()).addNextSong(songList.get(mposition));
                        break;
                    case 2:
                        ((MainActivity) getActivity()).addToQueue(songList.get(mposition));
                        break;
                    case 3:
                        ((MainActivity) getActivity()).addNextSong(songList.get(mposition));
                        Intent i = new Intent(getContext(), AddToPlaylistActivity.class);
                        i.putExtra("Song", songList.get(mposition));
                        startActivity(i);
                        break;
                    case 4:
                        // Delete Song Code
                        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
                        b.setTitle(getString(R.string.deleteMessage));
                        b.setMessage(songList.get(mposition).getTitle());
                        b.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                String[] projection = {MediaStore.Audio.Media._ID};
                                String selection = MediaStore.Audio.Media.DATA + " = ?";
                                String[] selectionArgs = new String[]{songList.get(mposition).getData()};
                                Cursor musicCursor = getActivity().getContentResolver().query(musicUri, projection,
                                        selection, selectionArgs, null);

                                if (musicCursor.moveToFirst()) {
                                    try {

                                        long id = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                                        Uri deleteUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                                        File fdelete = new File(selectionArgs[0]);

                                        if (fdelete.exists()) {
                                            if (fdelete.delete()) {
                                                if (MainActivity.getInstance().getPlayingSong() == songList.get(mposition)) {
                                                    MainActivity.getInstance().songList.remove(mposition);
                                                    MainActivity.getInstance().setServiceList();
                                                    MainActivity.getInstance().playNext();
                                                } else if (MainActivity.getInstance().songList.contains(songList.get(mposition))) {
                                                    MainActivity.getInstance().songList.remove(songList.get(mposition));
                                                    MainActivity.getInstance().setServiceList();
                                                }
                                                getActivity().getContentResolver().delete(deleteUri, null, null);
                                                Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();

                                                Provider provider = new Provider(getActivity());
                                                provider.execute();

                                            } else {

                                                Toast.makeText(getContext(), "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                            }
                                        }


                                    } catch (Exception e) {
                                        Toast.makeText(getContext(), "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(getContext(), "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                }
                                musicCursor.close();
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog d = b.create();
                        d.show();
                        break;
                    case 5:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mposition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        getActivity().startActivity(Intent.createChooser(intent, "Share Via"));
                        break;
                    case 6:
                        songDetails(mposition);
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void songDetails(int pos) {
        View v = li.inflate(R.layout.info, null);
        TextView title, artist, album, composer, duration, location;
        title = v.findViewById(R.id.info_title);
        artist = v.findViewById(R.id.info_artist);
        album = v.findViewById(R.id.info_album);
        composer = v.findViewById(R.id.info_composer);
        duration = v.findViewById(R.id.info_duration);
        location = v.findViewById(R.id.info_location);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
    public void onDataUpdate() {
        if (songList.size() > 0) {
            songList.clear();
        }
        songList.addAll(MainActivity.provider.getSongListByName());
        adapter.setList(songList);
        adapter.notifyDataSetChanged();
    }

    public interface OnFragmentItemClicked {
        void OnFragmentItemClick(int position, ArrayList<Song> songsArrayList, boolean nowPlaying);
    }

    public interface OnFragmentItemLongClicked {
        void OnFragmentItemLongClick(int position, ArrayList<Song> songsArrayList);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
        ((MainActivity) getActivity()).unregisterDataUpdateListener(this);
    }
}
