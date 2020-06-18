package com.ravisharma.playbackmusic.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.Adapters.SongAdapter;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Provider.Provider;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AlbumSongsActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;

    ImageView albumArt, imgBack;
    TextView albumTitle, albumSong;
    FastScrollRecyclerView recyclerView;

    private ArrayList<Song> songList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_songs);

        songList = new ArrayList<Song>();

        albumArt = findViewById(R.id.albumArt);
        imgBack = findViewById(R.id.imgBack);
        albumTitle = findViewById(R.id.albumTitle);
        albumSong = findViewById(R.id.albumSong);
        recyclerView = findViewById(R.id.song_list);

        String albumId = getIntent().getExtras().getString("albumId");

        Provider p = new Provider(this);
        songList = p.getSongList(albumId);

        if (songList.size() > 0) {
            /*Song art code here*/
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.logo);
            requestOptions.error(R.drawable.logo);

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList.get(0).getArt()))
                    .into(albumArt);

            albumTitle.setText(songList.get(0).getAlbum());
        } else {
            finish();
            Toast.makeText(this, "No Song Found", Toast.LENGTH_SHORT).show();
        }

        albumSong.setText(songList.size() + " Song(s)");
        recyclerView.setHasFixedSize(true);

        SongAdapter ad = new SongAdapter(songList, this);
        recyclerView.setAdapter(ad);
        ad.setOnClick(this);
        ad.setOnLongClick(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adView = findViewById(R.id.banner_container_albumActivity);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
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
        String[] items = getResources().getStringArray(R.array.longPressItems);
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
                        AlbumSongsActivity.this.onItemClick(mposition);
                        break;
                    case 1:
                        Intent i = new Intent(AlbumSongsActivity.this, AddToPlaylistActivity.class);
                        i.putExtra("Song", songList.get(mposition));
                        startActivity(i);
                        break;
                    case 2:
                        // Delete Song Code
                        AlertDialog.Builder b = new AlertDialog.Builder(AlbumSongsActivity.this);
                        b.setTitle(getString(R.string.deleteMessage));
                        b.setMessage(songList.get(mposition).getTitle());
                        b.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                                String[] projection = {MediaStore.Audio.Media._ID};
                                String selection = MediaStore.Audio.Media.DATA + " = ?";
                                String[] selectionArgs = new String[]{songList.get(mposition).getData()};
                                Cursor musicCursor = getContentResolver().query(musicUri, projection,
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
                                                }
                                                else if(MainActivity.getInstance().songList.contains(songList.get(mposition))){
                                                    MainActivity.getInstance().songList.remove(songList.get(mposition));
                                                    MainActivity.getInstance().setServiceList();
                                                }
                                                getContentResolver().delete(deleteUri, null, null);
                                                Toast.makeText(AlbumSongsActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

                                                Provider provider = new Provider(MainActivity.getInstance());
                                                provider.execute();

                                                updateList(mposition);
                                            } else {
                                                Toast.makeText(AlbumSongsActivity.this, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                            }
                                        }


                                    } catch (Exception e) {
                                        Toast.makeText(AlbumSongsActivity.this, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(AlbumSongsActivity.this, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
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
                    case 3:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("audio/*");
                        Uri uri = Uri.parse(songList.get(mposition).getData());
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        startActivity(Intent.createChooser(intent, "Share Via"));
                        break;
                    case 4:
                        songDetails(mposition);
                        break;
                }
                alertDialog.dismiss();
            }
        });
    }

    private void updateList(int mposition) {
        songList.remove(mposition);
        if (songList.size() > 0) {
            recyclerView.getAdapter().notifyDataSetChanged();
        } else {
            finish();
        }
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
