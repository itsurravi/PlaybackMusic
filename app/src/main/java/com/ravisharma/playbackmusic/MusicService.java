package com.ravisharma.playbackmusic;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.support.v4.media.session.MediaSessionCompat;

import java.util.concurrent.TimeUnit;
import android.app.Notification;
import android.app.PendingIntent;
import android.widget.SeekBar;
import android.widget.TextView;
import com.ravisharma.playbackmusic.broadcast.NotificationHandler;
import com.ravisharma.playbackmusic.database.repository.LastPlayedRepository;
import com.ravisharma.playbackmusic.database.repository.MostPlayedRepository;
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.utils.UtilsKt;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    public static final int id = 123;
    public String CHANNEL_ID;
    private String songTitle, artist;

    protected int songPosn;
    private Song playingSong;
    private ArrayList<Song> songs;

    protected MediaPlayer player;

    boolean shuffle = false;
    boolean repeat = false;
    boolean repeat_one = false;
    boolean notification = false;
    boolean fromButton = true;

    private boolean isStarted = false;
    private int playingSongPosition = 0;

    protected SeekBar seekBar;
    private TextView mCurrentPosition;
    private TextView mTotalDuration;

    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;

    /*For Notifications*/
    Notification playerNotification;
    NotificationManagerCompat notificationManagerCompat;

    private MostPlayedRepository mostPlayedRepository;
    private LastPlayedRepository lastPlayedRepository;

    private final IBinder musicBind = new MusicBinder();

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
    public void onCreate() {
        //create the service
        super.onCreate();
        CHANNEL_ID = getString(R.string.music_Service);
        songTitle = "";
        artist = "";
        shuffle = false;
        songs = new ArrayList<>();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //initialize position
        songPosn = 0;
        initMusicPlayer();

        mostPlayedRepository = new MostPlayedRepository(this);
        lastPlayedRepository = new LastPlayedRepository(this);

        registerBecomingNoisyReceiver();
    }

    public void checkRepeat(boolean repeat, boolean repeat_one) {
        this.repeat_one = repeat_one;
        this.repeat = repeat;
    }

    public void setRepeat() {
        if (repeat && repeat_one) {
            player.setLooping(false);
            repeat = false;
            repeat_one = false;
        } else {
            if (repeat) {
                repeat_one = true;
                player.setLooping(true);
            } else {
                repeat = true;
            }
        }
    }

    public void setShuffle(boolean shuff) {
        shuffle = shuff;
    }

    public void shuffleOnOff() {
        shuffle = !shuffle;
    }

    public void initMusicPlayer() {
        //create player
        player = new MediaPlayer();
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        MainActivity.getInstance().sessionId = player.getAudioSessionId();

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs) {
        songs = theSongs;
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
        UtilsKt.setSongPosition(songPosn);
    }

    private void start() {
        player.start();
        mProgressRunner.run();
        togglePlayPauseNotification(true);
        MainActivity.getInstance().setPauseIcons();
    }

    private void pause() {
        player.pause();
        togglePlayPauseNotification(false);
        MainActivity.getInstance().setPlayIcons();
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!isPng()) {
                    if (ongoingCall) {
                        start();
                        ongoingCall = false;
                    }
                }
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (isPng()) {
                    pause();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (isPng()) {
                    player.setVolume(0.1f, 0.1f);
                    pause();
                    ongoingCall = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (isPng()) {
                    player.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    private void requestAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            mProgressRunner.run();
        }
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isPng()) {
                pause();
            }
        }
    };

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    public void setPlayingPosition(String playingDuration) {
        playingSongPosition = Integer.parseInt(playingDuration);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong() {
        //play a adap_song
        player.reset();
        //get adap_song
        MainActivity.getInstance().trackCounterCheck();

        Song playSong = songs.get(songPosn);
        songTitle = playSong.getTitle();
        //get id
        long currSong = playSong.getId();
        //get Artist name
        artist = playSong.getArtist();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

        playingSong = playSong;
        try {
            player.setDataSource(getApplicationContext(), trackUri);

            player.prepare();
            UtilsKt.setPlayingSong(playSong);

            MainActivity.getInstance().songPosn = songPosn;

            mostPlayedRepository.addSongToMostPlayed(playSong);
            lastPlayedRepository.addSongToLastPlayed(playSong);
        } catch (Exception ignored) {

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        requestAudioFocus();
        if(!isStarted){
            mp.seekTo(playingSongPosition);
            isStarted = true;
        }

        mp.start();

        if (repeat_one) {
            mp.setLooping(true);
        }
        seekBar.setMax(mp.getDuration());
        seekBar.postDelayed(mProgressRunner, 100);
        mTotalDuration.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration()))
        ));
        createMediaStyleNotification();
        notification = true;
    }

    public void updateNotification() {
        if (player != null) {
            if (isPng()) {
                togglePlayPauseNotification(true);
            } else if (!isPng()) {
                togglePlayPauseNotification(false);
            }
        }
    }

    public void updateFavNotification(boolean check) {
        if (player != null && notification) {
            if (check) {
                playerNotification.actions[0] = new Notification.Action(R.drawable.ic_favorite_24, "Favorite", retrievePlaybackIntent(1));
            } else {
                playerNotification.actions[0] = new Notification.Action(R.drawable.ic_favorite_not_24, "Favorite", retrievePlaybackIntent(1));
            }
            notificationManagerCompat.notify(id, playerNotification);
        }
    }

    private void createMediaStyleNotification() {
        notificationManagerCompat = NotificationManagerCompat.from(this);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        Bitmap artWork;

        try {
            mmr.setDataSource(songs.get(songPosn).getData());
            InputStream inputStream = null;
            if (mmr.getEmbeddedPicture() != null) {
                inputStream = new ByteArrayInputStream(mmr.getEmbeddedPicture());
            }

            mmr.release();

            if (inputStream != null) {
                artWork = BitmapFactory.decodeStream(inputStream);
            } else {
                artWork = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
            }
        } catch (Exception e) {
            artWork = BitmapFactory.decodeResource(this.getResources(), R.drawable.logo);
        }

        NotificationCompat.Action favorite;

        PlaylistRepository repository = new PlaylistRepository(this);
        long exist = repository.isSongExist(getString(R.string.favTracks), playingSong.getId());
        if (exist > 0) {
            favorite = new NotificationCompat.Action
                    .Builder(R.drawable.ic_favorite_24, "Favorite", retrievePlaybackIntent(1))
                    .build();
        } else {
            favorite = new NotificationCompat.Action
                    .Builder(R.drawable.ic_favorite_not_24, "Favorite", retrievePlaybackIntent(1))
                    .build();
        }

        NotificationCompat.Action previous = new NotificationCompat.Action
                .Builder(R.drawable.ic_previous_24, "Previous", retrievePlaybackIntent(2))
                .build();
        NotificationCompat.Action playPause = new NotificationCompat.Action
                .Builder(R.drawable.ic_pause_24, "PlayPause", retrievePlaybackIntent(3))
                .build();
        NotificationCompat.Action next = new NotificationCompat.Action
                .Builder(R.drawable.ic_next_24, "Next", retrievePlaybackIntent(4))
                .build();
        NotificationCompat.Action close = new NotificationCompat.Action
                .Builder(R.drawable.ic_close_24, "Close", retrievePlaybackIntent(5))
                .build();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        playerNotification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_music_note)
                .setContentTitle(songTitle)
                .setContentText(artist)
                .setLargeIcon(artWork)
                .addAction(favorite)
                .addAction(previous)
                .addAction(playPause)
                .addAction(next)
                .addAction(close)
                .setContentIntent(pendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 2, 3)
                        .setMediaSession(MediaSessionCompat.Token.fromToken(MainActivity.getInstance().mediaSession.getSessionToken())))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(id, playerNotification);
    }

    private void togglePlayPauseNotification(boolean playing) {
        if (playing) {
            playerNotification.actions[2] = new Notification.Action(R.drawable.ic_pause_24, "PlayPause", retrievePlaybackIntent(3));
        } else {
            playerNotification.actions[2] = new Notification.Action(R.drawable.ic_play_24, "PlayPause", retrievePlaybackIntent(3));
        }
        notificationManagerCompat.notify(id, playerNotification);
    }

    private PendingIntent retrievePlaybackIntent(int which) {
        Intent intent = new Intent(this, NotificationHandler.class);
        PendingIntent pendingIntent;
        switch (which) {
            case 1:
                // fav
                intent.putExtra(getString(R.string.doit), getString(R.string.favorite));
                pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            case 2:
                // previous
                intent.putExtra(getString(R.string.doit), getString(R.string.prev));
                pendingIntent = PendingIntent.getBroadcast(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            case 3:
                // playPause track
                intent.putExtra(getString(R.string.doit), getString(R.string.playPause));
                pendingIntent = PendingIntent.getBroadcast(this, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;

            case 4:
                // next track
                intent.putExtra(getString(R.string.doit), getString(R.string.next));
                pendingIntent = PendingIntent.getBroadcast(this, 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;

            case 5:
                // close app
                intent.putExtra(getString(R.string.doit), getString(R.string.close));
                pendingIntent = PendingIntent.getBroadcast(this, 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

    public void setUIControls(SeekBar mseekBar, TextView currentPosition, TextView totalDuration) {
        seekBar = mseekBar;
        mCurrentPosition = currentPosition;
        mTotalDuration = totalDuration;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seek(progress);
                }

                mCurrentPosition.setText(String.format("%d:%02d",
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

    public boolean isPng() {
        if (player != null) {
            return player.isPlaying();
        } else {
            return false;
        }
    }

    public void pausePlayer() {
        if (isPng()) player.pause();
        if (fromButton) {
            createMediaStyleNotification();
        }
        updateNotification();
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        if (!isPng()) {
            requestAudioFocus();
            player.start();
        }
        if (fromButton) {
            createMediaStyleNotification();
        }
        updateNotification();
    }

    //skip to previous
    public void playPrev() {
        songPosn--;
        if (songPosn < 0) songPosn = songs.size() - 1;
        UtilsKt.setSongPosition(songPosn);
        playSong();
    }

    //skip to next
    public void playNext() {
        songPosn++;
        if (songPosn >= songs.size()) songPosn = 0;
        UtilsKt.setSongPosition(songPosn);
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        player.stop();
        player.release();
        unregisterReceiver(becomingNoisyReceiver);
        removeAudioFocus();
        if (seekBar != null) {
            seekBar.removeCallbacks(mProgressRunner);
        }
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        System.exit(0);
    }
}