package com.ravisharma.playbackmusic.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SingleSongPlayActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private TextView tvSongTitle, tvSongArtist, tvCurrentPosition, tvTotalDuration;
    private ImageView ivSongThumb, btn_PlayPause;
    private SeekBar seekBar;

    private MediaPlayer player;
    private AudioManager audioManager;

    private FrameLayout adContainerView;
    private AdView adView;

    private int numActivity = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_song_play);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFinishOnTouchOutside(false);

        adContainerView = findViewById(R.id.banner_container_single_song);

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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvSongArtist = findViewById(R.id.tvSongArtist);
        tvTotalDuration = findViewById(R.id.tvTotalDuration);
        tvCurrentPosition = findViewById(R.id.tvCurrentPosition);
        ivSongThumb = findViewById(R.id.ivSongThumb);
        btn_PlayPause = findViewById(R.id.btn_PlayPause);
        seekBar = findViewById(R.id.seekBar);

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

        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = manager.getRunningTasks(10);

        numActivity = taskList.get(0).numActivities;

        for (ActivityManager.RunningTaskInfo info : taskList) {
            Log.d("ActivityName", info.numActivities + " top activity" + info.topActivity.getClassName());
        }
    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = CustomAdSize.getAdSize(this);
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    public void setPlayerUI(Uri uri) {
        Song song = null;

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
                song = getSong(MediaStore.Audio.AudioColumns.DATA + "=?", new String[]{songFile.getAbsolutePath()});
            }
        }
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

    protected Runnable mProgressRunner = new Runnable() {
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

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        seekBar.removeCallbacks(mProgressRunner);
        removeAudioFocus();
        if (numActivity > 1) {
            finish();
        } else {
            finishAndRemoveTask();
            System.exit(0);
        }
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

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                    player.pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (player.isPlaying()) {
                    player.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
        seekBar.removeCallbacks(mProgressRunner);
        removeAudioFocus();
        if (numActivity > 1) {
            finish();
        } else {
            finishAndRemoveTask();
            System.exit(0);
        }
    }


    private Song getSong(String selection, String[] args) {
        ContentResolver musicResolver = this.getContentResolver();

        Cursor musicCursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                selection, args, null);

        Log.d("Song", selection + " " + args[0]);
        try {
            if (musicCursor != null /*&& musicCursor.getCount() > 0 && musicCursor.moveToFirst()*/) {
                Log.d("Song", musicCursor.getCount() + "");
                musicCursor.moveToFirst();
                String[] ar = musicCursor.getColumnNames();
                for (String col : ar) {
                    Log.d("Song", "Col: " + col + " value: " + musicCursor.getString(musicCursor.getColumnIndex(col)) + "");
                }

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
                tvSongTitle.setText(thisTitle);
                tvSongArtist.setText(thisArtist);

                RequestOptions requestOptions = new RequestOptions();
                requestOptions.error(R.drawable.logo);
                Glide.with(getApplicationContext())
                        .setDefaultRequestOptions(requestOptions)
                        .load(albumArt)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivSongThumb);

                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        thisId);

                setMediaPlayer(trackUri);

            }
        } catch (Exception e) {
            Log.d("Error", e.toString());
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }
        return null;
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
}