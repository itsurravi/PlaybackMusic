package com.example.ravisharma.musicdemo1;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;
import android.Manifest;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.net.Uri;
import android.os.IBinder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements
        MediaPlayerControl, View.OnClickListener, SearchView.OnQueryTextListener,
        RecentAdded.OnFragmentItemClicked, RecentAdded.OnFragmentInteractionListener, RecentAdded.OnFragmentItemLongClicked,
        NameWise.OnFragmentInteractionListener, NameWise.OnFragmentItemClicked, NameWise.OnFragmentItemLongClicked
{

    static MainActivity activity;
    int checkedItem = 0;
    static String songName, songArtist, songId, lastSongId;
    static Boolean lastShuffle;
    protected MusicService musicSrv;
    private ViewPager viewPager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    protected TextView title, artist, currentPosition, totalDuration;
    protected CircleImageView cardImage;
    protected ImageView slideImage;
    protected ImageButton playpause, prev, next, shuffle, repeat, playPauseSlide;
    private SearchView mSearchView;
    private SlidingUpPanelLayout slidingLayout;
    public SeekBar seekBar;
    private Menu menu;
    private ArrayList<Song> songList;
    private int songPosn;
    private Intent playIntent;
    protected boolean musicBound=false, fromlist=false, sortByName=false, started=false, fromRecent=false,
            fromButton = false, played = false, paused=false, playbackPaused=false;
    public Handler handler;
    LinearLayout slidePanelTop;
    AlarmManager am;
    boolean TIMER=false;
    Intent i;
    PendingIntent pi;


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }
        else{
            getView();
        }
    }

    public void getView(){
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        musicSrv = new MusicService();
        activity=this;
        handler = new Handler();

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.vpager);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        songList = new ArrayList<Song>();
        slidingLayout = (SlidingUpPanelLayout)findViewById(R.id.sliding_layout);
        mSearchView = findViewById(R.id.searchView);
        seekBar = findViewById(R.id.seekBar);
        title = findViewById(R.id.txtSongName);
        artist = findViewById(R.id.txtSongArtist);
        playPauseSlide = findViewById(R.id.btn_PlayPause_slide);
        playpause = findViewById(R.id.btn_PlayPause);
        prev = findViewById(R.id.btn_Prev);
        next = findViewById(R.id.btn_Next);
        shuffle = findViewById(R.id.btn_Shuffle);
        repeat = findViewById(R.id.btn_repeat);
        cardImage=findViewById(R.id.cardImage);
        slideImage=findViewById(R.id.slideImage);
        currentPosition = findViewById(R.id.currentPosition);
        totalDuration = findViewById(R.id.totalDuration);
        slidePanelTop=findViewById(R.id.slidePanelTop);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("song data", MODE_PRIVATE);

        lastSongId = pref.getString("ID", null);
        lastShuffle = pref.getBoolean("Shuffle", false);
        Boolean start = pref.getBoolean("Started", false);
        Boolean sort = pref.getBoolean("sort", false);
        Boolean list = pref.getBoolean("Recent", false);
        fromRecent=list;
        sortByName=sort;
        started=start;
        if(lastSongId!=null){
            songPosn= Integer.parseInt(lastSongId);
        }

        if(lastShuffle){
            shuffle.setImageResource(R.drawable.ic_shuffle);
        }
        else{
            shuffle.setImageResource(R.drawable.ic_shuffle_off);
        }

        slidingLayout.addPanelSlideListener(onSlideListener());

        slidingLayout.getChildAt(1).setOnClickListener(null);

        playPauseSlide.setOnClickListener(this);

        playpause.setOnClickListener(this);

        next.setOnClickListener(this);

        prev.setOnClickListener(this);

        shuffle.setOnClickListener(this);

        repeat.setOnClickListener(this);

        slidePanelTop.setOnClickListener(this);

        setupSearchView();

    }

    public void rcnt(boolean a){
        fromRecent=a;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnFragmentItemClick(int position, ArrayList<Song> songsArrayList) {
        songList=songsArrayList;
        fromlist=true;
        songPosn = position;
        musicSrv.setList(songList);
        musicSrv.setSong(songPosn);
        musicSrv.playSong();
        if(playbackPaused){
            playbackPaused=false;
        }
        musicBound=true;
        played = true;
        started=true;
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        songId = String.valueOf(songPosn);
        songName = songList.get(songPosn).getTitle();
        songArtist = songList.get(songPosn).getArtist();
        title.setText(songName);
        artist.setText(songArtist);
        picassoWork(songPosn);
        cardImage.startAnimation(musicSrv.rotateAnimation);
    }

    @Override
    public void OnFragmentItemLongClick(int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(songList.get(position).getData());
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch(position)
            {
                case 0: return new RecentAdded();
                case 1: return new NameWise();
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Recent Added";
                case 1:
                    return "Tracks";
            }
            return null;
        }
    }

    public void btnplaypause(){
        if(fromlist){
            if(musicBound){
                musicBound=false;
                pause();

            }
            else{
                musicBound=true;
                start();
                played=true;
            }
        }
        else{
            if(musicBound){
                musicBound=false;
                if(!fromButton){
                    musicSrv.playSong();
                    fromButton=true;
                }
                start();
                played=true;

            }
            else{
                musicBound=true;
                pause();

            }
        }
    }

    public void putSongList(ArrayList<Song> list){
        songList=list;
    }

    @Override
    public void onClick(View v) {
        if(started){
            if(v==playPauseSlide || v==playpause){
                btnplaypause();
            }

            if(v==next){
                playNext();
            }

            if(v==prev){
                playPrev();
            }

            if(v==shuffle) {
                musicSrv.setShuffle();
                if(musicSrv.shuffle){
                    Toast.makeText(this, "Shuffle On", Toast.LENGTH_SHORT).show();
                    shuffle.setImageResource(R.drawable.ic_shuffle);
                    musicSrv.player.setLooping(false);
                    musicSrv.repeat=false;
                    repeat.setImageResource(R.drawable.ic_repeat_off);
                }
                else{
                    Toast.makeText(this, "Shuffle Off", Toast.LENGTH_SHORT).show();
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                }
            }

            if(v==repeat) {
                musicSrv.setRepeat();
                if(musicSrv.repeat){
                    Toast.makeText(this, "Repeat On", Toast.LENGTH_SHORT).show();
                    repeat.setImageResource(R.drawable.ic_repeat);
                    musicSrv.shuffle=false;
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                }
                else
                {
                    Toast.makeText(this, "Repeat Off", Toast.LENGTH_SHORT).show();
                    repeat.setImageResource(R.drawable.ic_repeat_off);
                }
            }
        }
        else{
            Toast.makeText(this, "Select Song From List", Toast.LENGTH_SHORT).show();
        }

        if(v==slidePanelTop){
            if(slidingLayout.getPanelState()== SlidingUpPanelLayout.PanelState.COLLAPSED){
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
            else if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }
    }

    private void setupSearchView(){
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(true);
    }

    private SlidingUpPanelLayout.PanelSlideListener onSlideListener(){
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }
            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(started){
                    if(newState == SlidingUpPanelLayout.PanelState.COLLAPSED){
                        title.setSelected(false);
                        artist.setSelected(false);
                        playPauseSlide.setVisibility(View.VISIBLE);
                        Picasso.with(getApplicationContext()).load(songList.get(songPosn).getArt()).error(R.drawable.ic_headset).into(slideImage);
                    }
                    else if(newState == SlidingUpPanelLayout.PanelState.EXPANDED){
                        if(musicSrv.player.isPlaying()){
                            title.setSelected(true);
                            artist.setSelected(true);
                        }
                        else{
                            title.setSelected(false);
                            artist.setSelected(false);
                        }
                        playPauseSlide.setVisibility(View.GONE);
                        slideImage.setImageResource(R.drawable.ic_headset);
                    }
                }
            }
        };
    }

    public void setNextTitle(){
        songPosn=musicSrv.songPosn;
        picassoWork(songPosn);
        title.setText(songList.get(songPosn).getTitle());
        artist.setText(songList.get(songPosn).getArtist());
        cardImage.startAnimation(musicSrv.rotateAnimation);

    }

    public void picassoWork(int songPosn){
        Picasso.with(getApplicationContext())
                .load(songList.get(songPosn).getArt())
                .error(R.drawable.logo)
                .into(cardImage);
        if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
            Picasso.with(getApplicationContext()).load(songList.get(songPosn).getArt()).error(R.drawable.ic_headset).into(slideImage);
        }
        else{
            playPauseSlide.setVisibility(View.GONE);
            slideImage.setImageResource(R.drawable.ic_headset);
        }
    }

    public static MainActivity getInstance(){
        return activity;
    }

    public void onCallIncoming(){
        playpause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
    }

    public void onCallDisConnected(){
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu=menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.equalizer:
                Intent i = new Intent(MainActivity.this, EqualizerActivity.class);
                startActivity(i);
                break;

            case R.id.timer:
                showTimer();
                break;

            case R.id.search:
                if(mSearchView.getVisibility()==View.GONE){
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
                    mSearchView.setAnimation(animation);
                    mSearchView.setVisibility(View.VISIBLE);

                }
                else if(mSearchView.getVisibility()==View.VISIBLE){
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                    mSearchView.setAnimation(animation);
                    mSearchView.setVisibility(View.GONE);
                    mSearchView.cancelPendingInputEvents();
                }
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void showTimer(){
        final CharSequence[] items = {" Off "," 30 Minutes "," 1 Hour "," 1 Hour 30 Minutes "," 2 hour "};
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Choose Timer");
        dialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        checkedItem=0;
                        setTimer(0);
                        Toast.makeText(MainActivity.this, "Timer Off", Toast.LENGTH_SHORT).show();
                        break;
                    case 1 :
                        checkedItem=1;
                        setTimer(30*60*1000);
                        Toast.makeText(MainActivity.this, "Timer Set To 30 Mins", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        checkedItem=2;
                        setTimer(60*60*1000);
                        Toast.makeText(MainActivity.this, "Timer Set To 1 Hour", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        checkedItem=3;
                        setTimer(90*60*1000);
                        Toast.makeText(MainActivity.this, "Timer Set To 1 hour 30 Min", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        checkedItem=4;
                        setTimer(120*60*1000);
                        Toast.makeText(MainActivity.this, "Timer Set To 2 Hour", Toast.LENGTH_SHORT).show();
                        break;
                }
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    private void setTimer(int time){
         i = new Intent(this, Timer.class);
         pi = PendingIntent.getBroadcast(this, 1234, i, 0);
         am = (AlarmManager) getSystemService(ALARM_SERVICE);
         if(time==0){
             am.cancel(pi);
             TIMER=false;
         }
         else {
             am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, pi);
             TIMER=true;
         }
     }

    public void setStartingList(ArrayList<Song> list){
        songList=list;

        title.setText(songList.get(songPosn).getTitle());
        artist.setText(songList.get(songPosn).getArtist());
        totalDuration.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(songPosn).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(songPosn).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(songPosn).getDuration()))));

        Picasso.with(getApplicationContext()).load(songList.get(songPosn).getArt()).error(R.drawable.ic_headset).into(cardImage);
        Picasso.with(getApplicationContext()).load(songList.get(songPosn).getArt()).error(R.drawable.ic_headset).into(slideImage);

    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            //get service
            musicSrv = binder.getService();
            //pass list
            if(lastSongId!=null){
                musicSrv.setList(songList);
                musicSrv.checkShuffle(lastShuffle);
                musicSrv.setSong(Integer.parseInt(lastSongId));
            }
            musicSrv.setUIControls(seekBar, currentPosition, totalDuration);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    //play next
    protected void playNext(){
        musicSrv.playNext();
        played=true;
        if(playbackPaused){
            playbackPaused=false;
        }

        songPosn=musicSrv.songPosn;
        if(fromlist){
            musicBound=true;
        }
        else {
            musicBound=false;
        }
        fromButton=true;
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        picassoWork(songPosn);
        title.setText(songList.get(songPosn).getTitle());
        artist.setText(songList.get(songPosn).getArtist());
    }

    //play previous
    protected void playPrev(){
        musicSrv.playPrev();
        played=true;
        if(playbackPaused){
            playbackPaused=false;
        }
        songPosn=musicSrv.songPosn;
        if(fromlist){
            musicBound=true;
        }
        else {
            musicBound=false;
        }
        fromButton=true;
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        picassoWork(songPosn);
        title.setText(songList.get(songPosn).getTitle());
        artist.setText(songList.get(songPosn).getArtist());
    }


    @Override
    protected void onPause(){
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(paused){
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if(slidingLayout !=null &&
                (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)){

            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else{
            if(mSearchView.getVisibility()==View.VISIBLE){
                mSearchView.clearFocus();
                mSearchView.setQuery("", true);
                menu.getItem(0).collapseActionView();
                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
                mSearchView.setAnimation(animation);
                mSearchView.setVisibility(View.GONE);
            }
            else{
                moveTaskToBack(true);
            }
        }

    }

    @Override
    protected void onDestroy() {
        if(played){
            SharedPreferences pref = getApplicationContext().getSharedPreferences("song data", MODE_PRIVATE);
            SharedPreferences.Editor sp = pref.edit();
            sp.putBoolean("Key_name", true);
            sp.putString("ID", String.valueOf(songPosn));
            sp.putBoolean("Shuffle", musicSrv.shuffle);
            sp.putBoolean("Started", started);
            sp.putBoolean("Recent", fromRecent);
            sp.putBoolean("sort", sortByName);
            sp.commit();
        }
        if(TIMER){
            am.cancel(pi);
        }
        if (musicBound){
            unbindService(musicConnection);
            stopService(playIntent);
            musicSrv=null;
        }
        System.exit(0);
        super.onDestroy();
    }

    /*
    Media Player Control Methods
    */

    @Override
    public void start() {
        musicSrv.go();
        if(slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
            title.setSelected(true);
            artist.setSelected(true);
        }
        else if(slidingLayout.getPanelState()==SlidingUpPanelLayout.PanelState.COLLAPSED){
            title.setSelected(false);
            artist.setSelected(false);
        }
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        cardImage.startAnimation(musicSrv.rotateAnimation);
        musicSrv.mProgressRunner.run();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        title.setSelected(false);
        artist.setSelected(false);
        musicSrv.pausePlayer();
        playpause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        cardImage.clearAnimation();
    }

    @Override
    public int getDuration() {
        return musicSrv.getDur();

    }

    @Override
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    /*
    *  Permissions Checking
    * */

    public void checkPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            getView();
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    getView();
                }
                else{
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                        finish();
                    }
                    else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setMessage("You have forcefully denied Read storage permission.\n\nThis is Necessary and Important for the working of this APP."+"\n\n"+"Click on 'Grant' to grant permission")
                                .setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        finish();
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton("Don't", new DialogInterface.OnClickListener() {
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


    /*
    *  SearchView Work
    * */

    @Override
    public boolean onQueryTextSubmit(String newText) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
//        recyclerView.setAdapter(songAdt);
        return false;
    }

}
