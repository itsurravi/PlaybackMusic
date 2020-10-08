package com.ravisharma.playbackmusic.fragments;

import android.app.RecoverableSecurityException;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.activities.AddToPlaylistActivity;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.provider.SongsProvider;
import com.ravisharma.playbackmusic.utils.UtilsKt;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.adapters.SongAdapter;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class NameWise extends Fragment implements SongAdapter.OnItemClicked,
        SongAdapter.OnItemLongClicked {

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_name_wise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        songList = new ArrayList<>();

        recyclerView = v.findViewById(R.id.song_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        SongsProvider.Companion.getSongListByName().observe(this, new Observer<ArrayList<Song>>() {
            @Override
            public void onChanged(ArrayList<Song> songs) {
                if (songs.size() > 0) {
                    songList.clear();
                    songList.addAll(songs);

                    adapter.notifyDataSetChanged();
                }
            }
        });

        adapter = new SongAdapter(songList, getContext());
        recyclerView.setAdapter(adapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        adapter.setOnClick(this);
        adapter.setOnLongClick(this);

    }

    @Override
    public void onItemClick(int position) {
        OnFragmentItemClicked onFragmentItemClicked = (OnFragmentItemClicked) getActivity();
        onFragmentItemClicked.OnFragmentItemClick(position, songList, false);
    }

    @Override
    public void onItemLongClick(final int mPosition) {
        String[] items = getResources().getStringArray(R.array.longPressNameWise);
        ArrayAdapter<String> ad = new ArrayAdapter<String>(getContext(), R.layout.adapter_alert_list, items);

        LayoutInflater li = LayoutInflater.from(getActivity());

        View v = li.inflate(R.layout.alert_list, null);

        ListView lv = v.findViewById(R.id.list);
        TextView tv = v.findViewById(R.id.title);
        ImageView songArt = v.findViewById(R.id.songArt);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.logo);
        requestOptions.error(R.drawable.logo);

        Glide.with(v)
                .setDefaultRequestOptions(requestOptions)
                .load(songList.get(mPosition).getArt())
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(songArt);

        tv.setText(songList.get(mPosition).getTitle());
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
                    case 0: {
                        OnFragmentItemClicked onFragmentItemClicked = (OnFragmentItemClicked) getActivity();
                        onFragmentItemClicked.OnFragmentItemClick(mPosition, songList, false);
                    }
                    break;
                    case 1: {
                        ArrayList<Song> singleSong = new ArrayList<>();
                        singleSong.add(songList.get(mPosition));
                        OnFragmentItemClicked itemClicked = (OnFragmentItemClicked) getActivity();
                        itemClicked.OnFragmentItemClick(0, singleSong, false);
                    }
                    break;
                    case 2: {
                        UtilsKt.addNextSongToPlayingList(songList.get(mPosition));
                    }
                    break;
                    case 3: {
                        UtilsKt.addSongToPlayingList(songList.get(mPosition));
                    }
                    break;
                    case 4: {
                        Intent i = new Intent(getContext(), AddToPlaylistActivity.class);
                        i.putExtra("Song", songList.get(mPosition));
                        startActivity(i);
                    }
                    break;
                    case 5: {
                        // Delete Song Code
                        AlertDialog.Builder b = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
                        b.setTitle(getString(R.string.deleteMessage));
                        b.setMessage(songList.get(mPosition).getTitle());
                        b.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (songList.size() == 1) {
                                    Toast.makeText(getContext(), "Can't Delete Last Song", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                String[] projection = {MediaStore.Audio.Media._ID};
                                String selection = MediaStore.Audio.Media.DATA + " = ?";
                                String[] selectionArgs = new String[]{songList.get(mPosition).getData()};
                                Cursor musicCursor = getActivity().getContentResolver().query(musicUri, projection,
                                        selection, selectionArgs, null);

                                if (musicCursor.moveToFirst()) {
                                    long id = musicCursor.getLong(musicCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                                    Uri deleteUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                                    try {
                                        File fdelete = new File(selectionArgs[0]);
                                        if (fdelete.exists()) {
                                            getActivity().getContentResolver().delete(deleteUri, null, null);
                                            updateList(mPosition);
                                            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                            RecoverableSecurityException recoverableSecurityException;
                                            if (e instanceof RecoverableSecurityException) {
                                                recoverableSecurityException = (RecoverableSecurityException) e;
                                                IntentSender intentSender = recoverableSecurityException.getUserAction()
                                                        .getActionIntent().getIntentSender();
                                                try {
                                                    UtilsKt.setDeleteUri(deleteUri);
                                                    startIntentSenderForResult(intentSender, 20123,
                                                            null, 0, 0, 0, null);
                                                } catch (IntentSender.SendIntentException ex) {
                                                    ex.printStackTrace();
                                                }
                                            } else {
                                                Toast.makeText(getContext(), "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                            }
                                        }
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
                    }
                    break;
                    case 6: {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mPosition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        getActivity().startActivity(Intent.createChooser(intent, "Share Via"));
                    }
                    break;
                    case 7: {
                        songDetails(mPosition);
                    }
                    break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void songDetails(int pos) {
        LayoutInflater li = LayoutInflater.from(getActivity());

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

    public interface OnFragmentItemClicked {
        void OnFragmentItemClick(int position, ArrayList<Song> songsArrayList, boolean nowPlaying);
    }

    public interface OnFragmentItemLongClicked {
        void OnFragmentItemLongClick(int position, ArrayList<Song> songsArrayList);
    }

    private void updateList(int mposition) {
        Song song = songList.get(mposition);
        if (song.equals(UtilsKt.getPlayingSong().getValue())) {
            MainActivity.getInstance().playNext();
        }
        UtilsKt.removeFromPlayingList(song);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

    }
}
