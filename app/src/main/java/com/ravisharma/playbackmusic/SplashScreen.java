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

import com.ravisharma.playbackmusic.database.PlaylistRepository;
import com.ravisharma.playbackmusic.database.model.DatabaseSetup;
import com.ravisharma.playbackmusic.provider.Provider;


public class SplashScreen extends AppCompatActivity {

    public static boolean shown = false;
    public String CHANNEL_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        CHANNEL_ID = getString(R.string.music_Service);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int imp = NotificationManager.IMPORTANCE_HIGH;

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
        MainActivity.provider = new Provider(this);
        MainActivity.provider.execute();
    }

    public void runIntent() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PlaylistRepository repository = new PlaylistRepository(SplashScreen.this);
                repository.isDatabaseRead().observe(SplashScreen.this, new Observer<DatabaseSetup>() {
                    @Override
                    public void onChanged(DatabaseSetup databaseSetup) {
                        if(databaseSetup.isSetup()){
                            startActivity(new Intent(SplashScreen.this, MainActivity.class));
                            finish();
                        }
                    }
                });
            }
        }, 2000);
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
