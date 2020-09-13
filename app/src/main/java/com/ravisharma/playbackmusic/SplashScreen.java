package com.ravisharma.playbackmusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

import com.ravisharma.playbackmusic.database.PlaylistRepository;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.prefrences.PrefManager;
import com.ravisharma.playbackmusic.prefrences.TinyDB;
import com.ravisharma.playbackmusic.provider.SongsProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SplashScreen extends AppCompatActivity {

    public String CHANNEL_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        CHANNEL_ID = getString(R.string.music_Service);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int imp = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel c = new NotificationChannel(CHANNEL_ID, getString(R.string.PlaybackMusicService), imp);
            c.setSound(null, null);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(c);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            runTask();
        }
    }

    private void runTask() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SongsProvider provider = new SongsProvider();

                provider.fetchAllData(getContentResolver()).observe(SplashScreen.this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean aBoolean) {
                        if (aBoolean) {
                            checkInPlaylists();
                            runIntent();
                        }
                    }
                });
            }
        }, 1000);
    }

    private void checkInPlaylists() {
        ArrayList<Song> songListByName = SongsProvider.Companion.getSongListByName().getValue();
        if (songListByName != null) {
            if (songListByName.size() <= 1) {
                PrefManager manage = new PrefManager(this);
                manage.storeInfo(getString(R.string.ID), "remove");
                manage.storeInfo(getString(R.string.Shuffle), false);
                manage.storeInfo(getString(R.string.Repeat), false);
                manage.storeInfo(getString(R.string.RepeatOne), false);
                manage.storeInfo(getString(R.string.Started), false);
                manage.storeInfo("position", "remove");
            }
            TinyDB tinydb = new TinyDB(this);

            PlaylistRepository repository = new PlaylistRepository(this);

            List<String> playListArrayList = new ArrayList<>();
            playListArrayList.add("NormalSongs");
            playListArrayList.add("Songs");

            for (String playListName : playListArrayList) {
                ArrayList<Song> songList = tinydb.getListObject(playListName, Song.class);

                for (Iterator<Song> iterator = songList.iterator(); iterator.hasNext(); ) {
                    Song value = iterator.next();
                    if (!songListByName.contains(value)) {
                        iterator.remove();
                    }
                }
                if (playListName.equals("Songs") && songList.size() == 0) {

                    PrefManager manage = new PrefManager(this);
                    manage.storeInfo(getString(R.string.Songs), false);
                }
                tinydb.putListObject(playListName, songList);
            }

            List<Playlist> playlists = repository.getAllPlaylistSongs();

            for (Playlist playlist : playlists) {
                Song s = playlist.getSong();
                if (!songListByName.contains(s)) {
                    repository.removeSong(s.getId());
                }
            }
        }
    }

    public void runIntent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        }, 1000);
    }

    /*
     *  Permissions Checking
     * */

    public void checkPermission() {
        if ((ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED)) {
            runTask();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.WAKE_LOCK,
                    android.Manifest.permission.FOREGROUND_SERVICE,
                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runTask();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.permissionAlert))
                            .setPositiveButton(getString(R.string.Grant), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                            Uri.fromParts(getString(R.string.packageName), getPackageName(), null));
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                }
                            })
                            .setNegativeButton(getString(R.string.dont), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    finish();
                                }
                            });
                    builder.setCancelable(false);
                    builder.create().show();
                }
            }
        }
    }
}
