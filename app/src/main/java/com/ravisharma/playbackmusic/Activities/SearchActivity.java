package com.ravisharma.playbackmusic.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.Adapters.SongAdapter;
import com.ravisharma.playbackmusic.Fragments.NameWise;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Provider.Provider;
import com.ravisharma.playbackmusic.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class SearchActivity extends AppCompatActivity implements SongAdapter.OnItemClicked, SongAdapter.OnItemLongClicked {

    private AdView adView;

    TextView tvSearch;
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
        setContentView(R.layout.activity_search);

        tvSearch = findViewById(R.id.tvSearch);
        edSearch = findViewById(R.id.edSearch);
        imgBack = findViewById(R.id.imgBack);
        recyclerView = findViewById(R.id.song_list);

        songList = new ArrayList<>();

        initRecyclerView();

        initListeners();

        edSearch.requestFocus();

        // Instantiate an AdView object.
        // NOTE: The placement ID from the Facebook Monetization Manager identifies your App.
        // To get test ads, add IMG_16_9_APP_INSTALL# to your placement id. Remove this when your app is ready to serve real ads.

        adView = findViewById(R.id.banner_container_search);
        AdRequest adRequest = new AdRequest.Builder().build();
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
                    searchName = name;
                    new MySearch().execute();
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
                searchName = name;
                new MySearch().execute();
            }
        });

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                        SearchActivity.this.onItemClick(mposition);
                        break;
                    case 1:
                        Intent i = new Intent(SearchActivity.this, AddToPlaylistActivity.class);
                        i.putExtra("Song", songList.get(mposition));
                        startActivity(i);
                        break;
                    case 2:
                        // Delete Song Code
                        AlertDialog.Builder b = new AlertDialog.Builder(SearchActivity.this);
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
                                                songList.remove(mposition);
                                                adapter.notifyDataSetChanged();
                                                getContentResolver().delete(deleteUri, null, null);
                                                Toast.makeText(SearchActivity.this, "Deleted", Toast.LENGTH_SHORT).show();

                                                Provider provider = new Provider(MainActivity.getInstance());
                                                provider.execute();
                                            } else {

                                                Toast.makeText(SearchActivity.this, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                            }
                                        }


                                    } catch (Exception e) {

                                        Toast.makeText(SearchActivity.this, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(SearchActivity.this, "Can't Delete. Try Manually", Toast.LENGTH_SHORT).show();
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
                    MediaStore.Audio.Media.TITLE + " LIKE '%" + searchName + "%' OR " +
                            MediaStore.Audio.Media.ALBUM + " LIKE '%" + searchName + "%' OR " +
                            MediaStore.Audio.Media.ARTIST + " LIKE '%" + searchName + "%'",
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