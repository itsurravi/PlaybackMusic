package com.example.ravisharma.musicdemo1;

/**
 * Created by Ravi Sharma on 07-Jan-18.
 */

import java.io.IOException;
import java.util.ArrayList;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import android.app.Notification;
import android.app.PendingIntent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.Picasso;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{


    protected MainActivity act;
    //media player
    protected MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    protected int songPosn;
    private String songTitle="", artist="";
    boolean shuffle=false;
    boolean repeat=false;
    private Random random;
    protected SeekBar seekBar;
    private TextView mCurrentPosition;
    private TextView mTotalDuration;
    private AudioManager audioManager;
    Animation rotateAnimation;
    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    RemoteViews smallView, bigView;
    NotificationCompat.Builder builder;
    Notification not;
    NotificationManager notificationManager;
    boolean fromButton=true;




    private final IBinder musicBind = new MusicBinder();

    protected Runnable mProgressRunner = new Runnable() {
        @Override
        public void run() {
            try{
                if(seekBar != null){
                    seekBar.setProgress(player.getCurrentPosition());
                    seekBar.postDelayed(mProgressRunner, 100);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate(){
        //create the service
        super.onCreate();
        act = new MainActivity();
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_anim);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //initialize position
        songPosn=0;
        initMusicPlayer();
        random =new Random();
        registerBecomingNoisyReceiver();
        callStateListener();
    }

    public void checkShuffle(boolean shuff){
        shuffle=shuff;
    }

    public void setRepeat(){
        if(repeat) {
            player.setLooping(false);
            repeat=false;
        }
        else{
            player.setLooping(true);
            repeat=true;
        }
    }

    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

    public void initMusicPlayer(){
        //create player
        player = new MediaPlayer();
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    private void start(){
        player.start();
        MainActivity.getInstance().cardImage.startAnimation(rotateAnimation);
        mProgressRunner.run();
        bigView.setImageViewResource(R.id.status_bar_ex_play, R.drawable.uamp_ic_pause_white_24dp);
        smallView.setImageViewResource(R.id.status_bar_play, R.drawable.uamp_ic_pause_white_24dp);
        MainActivity.getInstance().onCallDisConnected();
        notificationManager.notify(123, not);
    }

    private void pause(){
        player.pause();
        MainActivity.getInstance().cardImage.clearAnimation();
        bigView.setImageViewResource(R.id.status_bar_ex_play, R.drawable.uamp_ic_play_arrow_white_24dp);
        smallView.setImageViewResource(R.id.status_bar_play, R.drawable.uamp_ic_play_arrow_white_24dp);
        MainActivity.getInstance().onCallIncoming();
        notificationManager.notify(123, not);
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!player.isPlaying()){
                    MainActivity.getInstance().playpause.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!player.isPlaying()){
                                start();
                                Toast.makeText(MusicService.this, "start", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                pause();
                                Toast.makeText(MusicService.this, "pause", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    MainActivity.getInstance().playPauseSlide.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!player.isPlaying()){
                                start();
                            }
                            else {
                                pause();
                            }
                        }
                    });
                    if(ongoingCall){
                        start();
                        ongoingCall=false;
                    }
                }
                player.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (player.isPlaying()) pause();

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player.isPlaying()){
                    player.setVolume(0.1f, 0.1f);
                    pause();
                    ongoingCall=true;
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player.isPlaying()) player.setVolume(0.1f, 0.1f);
                break;
        }
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
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void playSong(){
        //play a song
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);
        songTitle=playSong.getTitle();
        //get id
        long currSong = playSong.getID();
        //get Artist name
        artist = playSong.getArtist();
        //set uri
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                currSong);

//        Toast.makeText(this, String.valueOf(trackUri), Toast.LENGTH_SHORT).show();
        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        try {
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
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
    public boolean onUnbind(Intent intent){

        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition() >0){
            mp.reset();
            playNext();
            MainActivity.getInstance().setNextTitle();
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
        mp.start();
        if(repeat){
            mp.setLooping(true);
        }
        seekBar.setMax(mp.getDuration());
        seekBar.postDelayed(mProgressRunner, 100);
        MainActivity.getInstance().cardImage.startAnimation(rotateAnimation);
        mTotalDuration.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(player.getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(player.getDuration()))
        ));
        notification();
    }

    public void updateNotification(){
        if(player!=null){
            if(player.isPlaying()){
                bigView.setImageViewResource(R.id.status_bar_ex_play, R.drawable.uamp_ic_pause_white_24dp);
                smallView.setImageViewResource(R.id.status_bar_play, R.drawable.uamp_ic_pause_white_24dp);
            }
            else if(!player.isPlaying()){
                bigView.setImageViewResource(R.id.status_bar_ex_play, R.drawable.uamp_ic_play_arrow_white_24dp);
                smallView.setImageViewResource(R.id.status_bar_play, R.drawable.uamp_ic_play_arrow_white_24dp);
            }
            notificationManager.notify(123, not);
        }
    }

    public void notification(){
        fromButton=false;

        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        smallView = new RemoteViews(getPackageName(), R.layout.status_bar);
        bigView = new RemoteViews(getPackageName(), R.layout.status_bar_expanded);


        smallView.setImageViewResource(R.id.status_bar_albumart, R.drawable.ic_headset);
        smallView.setTextViewText(R.id.status_bar_track_name, songTitle);
        smallView.setTextViewText(R.id.status_bar_artist_name, artist);


        bigView.setImageViewResource(R.id.status_bar_ex_album_art, R.drawable.ic_headset);
        bigView.setTextViewText(R.id.status_bar_ex_track_name, songTitle);
        bigView.setTextViewText(R.id.status_bar_ex_artist_name, artist);


        Intent previousIntent = new Intent(this, NotificationHandler.class);
        previousIntent.putExtra("do", "prev");
        PendingIntent ppreviousIntent = PendingIntent.getBroadcast(this, 9, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent playPauseIntent = new Intent(this, NotificationHandler.class);
        playPauseIntent.putExtra("do", "playPause");
        PendingIntent pplayPauseIntent = PendingIntent.getBroadcast(this, 8, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent nextIntent = new Intent(this, NotificationHandler.class);
        nextIntent.putExtra("do", "next");
        PendingIntent pnextIntent = PendingIntent.getBroadcast(this, 7, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Intent intent = new Intent(this, NotificationHandler.class);
        intent.putExtra("do", "close");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 6, intent,PendingIntent.FLAG_UPDATE_CURRENT);


        smallView.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        smallView.setOnClickPendingIntent(R.id.status_bar_play, pplayPauseIntent);
        smallView.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        smallView.setOnClickPendingIntent(R.id.status_bar_collapse, pendingIntent);

        bigView.setOnClickPendingIntent(R.id.status_bar_ex_prev, ppreviousIntent);
        bigView.setOnClickPendingIntent(R.id.status_bar_ex_play, pplayPauseIntent);
        bigView.setOnClickPendingIntent(R.id.status_bar_ex_next, pnextIntent);
        bigView.setOnClickPendingIntent(R.id.status_bar_ex_collapse, pendingIntent);


        Intent intent1 = new Intent(this, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(getApplicationContext(), 0,
                intent1, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(this);

        builder.setSmallIcon(R.drawable.ic_headset_small)
                .setCustomContentView(smallView)
                .setCustomBigContentView(bigView)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH+10)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentIntent(pendingIntent1);

        bigView.setImageViewResource(R.id.status_bar_ex_play, R.drawable.uamp_ic_pause_white_24dp);
        smallView.setImageViewResource(R.id.status_bar_play, R.drawable.uamp_ic_pause_white_24dp);
        not = builder.build();

        Picasso
                .with(this)
                .load(songs.get(songPosn).getArt())
                .into(bigView, R.id.status_bar_ex_album_art, 123, not);
        Picasso
                .with(this)
                .load(songs.get(songPosn).getArt())
                .into(smallView, R.id.status_bar_albumart, 123, not);

        startForeground(123, not);

    }

    public void setUIControls(SeekBar mseekBar, TextView currentPosition, TextView totalDuration){
        seekBar=mseekBar;
        mCurrentPosition = currentPosition;
        mTotalDuration = totalDuration;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
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

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        if(player.isPlaying()) player.pause();
        if(fromButton){
            notification();
        }
        updateNotification();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        if(!player.isPlaying()){
            requestAudioFocus();
            player.start();
        }
        if(fromButton){
            notification();
        }
        updateNotification();
    }

    //skip to previous
    public void playPrev(){
        songPosn--;
        if(songPosn<0) songPosn=songs.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong= random.nextInt(songs.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn>=songs.size()) songPosn=0;
        }
        playSong();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        player.stop();
        player.release();
        unregisterReceiver(becomingNoisyReceiver);
        removeAudioFocus();
        super.onDestroy();
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pausePlayer();
            MainActivity.getInstance().onCallIncoming();
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if(player.isPlaying()){
                    switch (state) {
                        //if at least one call exists or the phone is ringing
                        //pause the MediaPlayer
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                            if(player.isPlaying()){
                                player.setVolume(0.5f, 0.5f);
//                                pause();
                                Toast.makeText(MusicService.this, "working", Toast.LENGTH_SHORT).show();
                            }
                            ongoingCall = true;
                            break;
                        case TelephonyManager.CALL_STATE_IDLE:
                            // Phone idle. Start playing.
                            if (ongoingCall) {
                                ongoingCall = false;
                                start();
                            }
                            break;
                    }
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

}