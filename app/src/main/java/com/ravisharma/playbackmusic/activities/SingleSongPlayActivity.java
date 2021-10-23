package com.ravisharma.playbackmusic.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.R;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SingleSongPlayActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private TextView tvSongTitle, tvSongArtist, tvCurrentPosition, tvTotalDuration;
    private ImageView ivSongThumb, btn_PlayPause;
    private SeekBar seekBar;
    private SpinKitView progressBar;

    private Runnable mProgressRunner;

    private MediaPlayer player;
    private AudioManager audioManager;

    private LinearLayout infoLayout;
    private FrameLayout adContainerView;
    private AdView adView;

    private int numActivity = 0;
    private boolean played = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_song_play);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        this.setFinishOnTouchOutside(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        } else {
            runTask();
        }
    }

    private void runTask() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvSongArtist = findViewById(R.id.tvSongArtist);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        ivSongThumb = findViewById(R.id.ivSongThumb);
        btn_PlayPause = findViewById(R.id.btn_PlayPause);
        seekBar = findViewById(R.id.seekBar);
        infoLayout = findViewById(R.id.infoLayout);
        adContainerView = findViewById(R.id.banner_container_single_song);

        progressBar = findViewById(R.id.spin_kit);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.SingleSongActId));
        adContainerView.addView(adView);
        loadBanner();

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adContainerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                adContainerView.setVisibility(View.GONE);
            }
        });


        final Intent data = getIntent();

        setPlayerUI(data.getData());

        btn_PlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player == null) {
                    setMediaPlayer(data.getData());
                } else {
                    if (player.isPlaying()) {
                        btn_PlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
                        player.pause();
                    } else {
                        requestAudioFocus();
                        btn_PlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
                        player.start();
                    }
                }
            }
        });

        mProgressRunner = new Runnable() {
            @Override
            public void run() {
                try {
                    if (seekBar != null) {
                        seekBar.setProgress(player.getCurrentPosition());
                        seekBar.postDelayed(mProgressRunner, 100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = manager.getRunningTasks(10);

        numActivity = taskList.get(0).numActivities;

    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = AdSize.BANNER;
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    private void setMediaPlayer(Uri songUri) {
        player = new MediaPlayer();
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);

        try {
            player.setDataSource(getApplicationContext(), songUri);
        } catch (Exception ignored) {

        }
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    player.seekTo(progress);
                }
                tvCurrentPosition.setText(String.format("%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(progress),
                        TimeUnit.MILLISECONDS.toSeconds(progress) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(progress))
                ));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            mProgressRunner.run();
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp.getCurrentPosition() > 0) {
            mp.reset();
            seekBar.setProgress(0);
            btn_PlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
            player = null;
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        played = true;
        requestAudioFocus();
        mp.start();
        btn_PlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        seekBar.setMax(mp.getDuration());
        seekBar.postDelayed(mProgressRunner, 100);
        tvTotalDuration.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration()))
        ));
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!player.isPlaying()) {
                    player.start();
                    btn_PlayPause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
                }
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (player.isPlaying()) {
                    player.pause();
                    btn_PlayPause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
                }
                killApp();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                    player.pause();
                }
                killApp();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (played) {
            killApp();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        killApp();
    }

    private void killApp() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        if (seekBar != null) {
            seekBar.removeCallbacks(mProgressRunner);
            seekBar = null;
        }
        if (mProgressRunner != null) {
            mProgressRunner = null;
        }
        if (audioManager != null) {
            removeAudioFocus();
            audioManager = null;
        }
        if (numActivity > 1) {
            finish();
        } else {
            finishAndRemoveTask();
            System.exit(0);
        }
    }

    public void setPlayerUI(Uri uri) {
        Log.d("SongURI", uri.toString() + "");
        new FetchSongInfo(getContentResolver()).execute(uri);
    }

    private class MediaFile {
        Uri trackUri;
        String thisTitle;
        String thisArtist;
        Uri albumArt;
    }

    private class FetchSongInfo extends AsyncTask<Uri, Void, MediaFile> {

        ContentResolver resolver;

        public FetchSongInfo(ContentResolver resolver) {
            this.resolver = resolver;
        }

        private MediaFile getSong(String selection, String[] args) {
            MediaFile mediaFile = new MediaFile();

            ContentResolver musicResolver = resolver;

            Cursor musicCursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                    selection, args, null);

            Log.d("Song", selection + " " + args[0]);

            try {
                if (musicCursor != null /*&& musicCursor.getCount() > 0*/ && musicCursor.moveToFirst()) {
                    Log.d("Song", musicCursor.getCount() + "");

                    int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                    int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

                    long thisId = musicCursor.getLong(idColumn);
                    String thisTitle = musicCursor.getString(titleColumn);
                    String thisArtist = musicCursor.getString(artistColumn);
                    long thisAlbumAid = musicCursor.getLong(albumIdColumn);
                    final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                    Uri albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);

                    Uri trackUri = ContentUris.withAppendedId(
                            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            thisId);

                    mediaFile.albumArt = albumArt;
                    mediaFile.thisArtist = thisArtist;
                    mediaFile.thisTitle = thisTitle;
                    mediaFile.trackUri = trackUri;

                    musicCursor.close();
                }
                else{
                    return null;
                }
            } catch (Exception e) {
                Log.d("Error", e.toString());
                return null;
            }
            return mediaFile;
        }

        @Nullable
        private String getFilePathFromUri(Context context, Uri uri) {
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {
                    column
            };

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }

        private String getSongIdFromMediaProvider(Uri uri) {
            return DocumentsContract.getDocumentId(uri).split(":")[1];
        }

        @Override
        protected MediaFile doInBackground(Uri... uris) {
            MediaFile song = null;

            Uri uri = uris[0];

            if (uri.getScheme() != null && uri.getAuthority() != null) {
                if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                    String songId = null;
                    if (uri.getAuthority().equals("com.android.providers.media.documents")) {
                        songId = getSongIdFromMediaProvider(uri);
                    } else if (uri.getAuthority().equals("media")) {
                        songId = uri.getLastPathSegment();
                    }
                    if (songId != null) {
                        song = getSong(MediaStore.Audio.AudioColumns._ID + "=?", new String[]{songId});
                    }
                }
            }
            if (song == null) {
                File songFile = null;
                if (uri.getAuthority() != null && uri.getAuthority().equals("com.android.externalstorage.documents")) {
                    songFile = new File(Environment.getExternalStorageDirectory(), uri.getPath().split(":", 2)[1]);
                }
                if (songFile == null) {
                    String path = getFilePathFromUri(SingleSongPlayActivity.this, uri);
                    if (path != null)
                        songFile = new File(path);
                }
                if (songFile == null && uri.getPath() != null) {
                    songFile = new File(uri.getPath());
                }

                if (songFile != null) {
                    Log.d("Song", songFile.getAbsolutePath() + "");
                    song = getSong(MediaStore.Audio.AudioColumns.DATA + "=?", new String[]{songFile.getAbsolutePath()});
                }
            }
            return song;
        }

        @Override
        protected void onPostExecute(MediaFile mediaFile) {
            super.onPostExecute(mediaFile);
            if (mediaFile != null) {
                tvSongTitle.setText(mediaFile.thisTitle);
                tvSongArtist.setText(mediaFile.thisArtist);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.error(R.drawable.logo);
                Glide.with(getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(mediaFile.albumArt)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivSongThumb);

                setMediaPlayer(mediaFile.trackUri);

                progressBar.setVisibility(View.GONE);
                infoLayout.setVisibility(View.VISIBLE);

            } else {
                killApp();
                Toast.makeText(SingleSongPlayActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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