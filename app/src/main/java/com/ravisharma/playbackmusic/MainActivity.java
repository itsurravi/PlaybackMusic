package com.ravisharma.playbackmusic;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.ravisharma.playbackmusic.Activities.AboutActivity;
import com.ravisharma.playbackmusic.Activities.NowPlayingActivity;
import com.ravisharma.playbackmusic.Activities.SearchActivity;
import com.ravisharma.playbackmusic.Broadcast.Timer;
import com.ravisharma.playbackmusic.Fragments.AlbumsFragment;
import com.ravisharma.playbackmusic.Fragments.ArtistFragment;
import com.ravisharma.playbackmusic.Fragments.NameWise;
import com.ravisharma.playbackmusic.Fragments.PlaylistFragment;
import com.ravisharma.playbackmusic.Model.Song;
import com.ravisharma.playbackmusic.Prefrences.PrefManager;
import com.ravisharma.playbackmusic.Prefrences.TinyDB;
import com.ravisharma.playbackmusic.Provider.Provider;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
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
import android.widget.MediaController.MediaPlayerControl;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

public class MainActivity extends AppCompatActivity implements MediaPlayerControl,
        View.OnClickListener, NameWise.OnFragmentItemClicked {

    private FrameLayout adContainerView;
    private AdView adView;

    private static String TAG;
    String latestVersion, currentVersion;

    public static int d = 0;
    private List<DataUpdateListener> mListeners;

    public final static int ALBUM_SONGS = 1;
    public final static int ARTIST_SONGS = 2;
    public final static int NOW_PLAYING = 3;
    public final static int SEARCH_RESULT = 4;
    public final static int PLAYLIST = 5;
    public final static int RECENT_ADDED = 6;

    public static MainActivity activity;
    public static String songName, songArtist, songId, lastSongId;
    public static Boolean lastShuffle;
    public SeekBar seekBar;
    public MusicService musicSrv;

    public TextView title, artist, currentPosition, totalDuration;
    public Toolbar toolbar;
    public KenBurnsView control_back_image;
    public ImageView slideImage, cardImage;
    public ImageButton playpause, prev, next, shuffle, repeat, playPauseSlide, songInfo, playlist, favorite;
    public ViewPager viewPager;
    public SlidingUpPanelLayout slidingLayout;
    public LinearLayout slidePanelTop;
    public RelativeLayout player_controller;
    public Menu menu;
    public LayoutInflater li;

    public boolean musicBound = false, fromlist = false, started = false,
            fromButton = false, played = false, paused = false, playbackPaused = false, TIMER = false;


    public ArrayList<Song> songList;
    public int songPosn;
    private Intent playIntent, i;
    private PendingIntent pi;

    int checkedItem = 0;
    AlarmManager am;

    PrefManager manage;
    TinyDB tinydb;
    public static Provider provider;

    MediaSession mediaSession;

    Song playingSong;

    public static MainActivity getInstance() {
        return activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        TAG = getString(R.string.app_name);
        mListeners = new ArrayList<>();

        mediaSession = new MediaSession(getApplicationContext(), TAG);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(getResources().getColor(R.color.titleColor));
        toolbar.setNavigationIcon(R.drawable.toolbar_logo);
        setSupportActionBar(toolbar);

        getView();

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent intent) {
                KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event == null) {
                    return false;
                }
                int action = event.getAction();
                performAction(action);
                return super.onMediaButtonEvent(intent);
            }
        });

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY_PAUSE)
                .setState(PlaybackState.STATE_STOPPED, PlaybackState.PLAYBACK_POSITION_UNKNOWN, SystemClock.elapsedRealtime())
                .build();
        mediaSession.setPlaybackState(state);

        mediaSession.setActive(true);
    }

    public void getView() {
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
        }

        SectionsPagerAdapter sectionsPagerAdapter;

        player_controller = (RelativeLayout) findViewById(R.id.player_cotroller);
        player_controller.setVisibility(View.INVISIBLE);
        musicSrv = new MusicService();

        li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.vpager);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        songList = new ArrayList<Song>();

        control_back_image = findViewById(R.id.control_back);
        slidingLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        seekBar = findViewById(R.id.seekBar);
        title = findViewById(R.id.txtSongName);
        artist = findViewById(R.id.txtSongArtist);
        playPauseSlide = findViewById(R.id.btn_PlayPause_slide);
        playpause = findViewById(R.id.btn_PlayPause);
        prev = findViewById(R.id.btn_Prev);
        next = findViewById(R.id.btn_Next);
        shuffle = findViewById(R.id.btn_Shuffle);
        repeat = findViewById(R.id.btn_repeat);
        cardImage = findViewById(R.id.cardImage);
        slideImage = findViewById(R.id.slideImage);
        currentPosition = findViewById(R.id.currentPosition);
        totalDuration = findViewById(R.id.totalDuration);
        slidePanelTop = findViewById(R.id.slidePanelTop);
        songInfo = (ImageButton) findViewById(R.id.currentSongInfo);
        playlist = (ImageButton) findViewById(R.id.imgPlaylist);
        favorite = (ImageButton) findViewById(R.id.imgFav);

        manage = new PrefManager(getApplicationContext());
        tinydb = new TinyDB(getApplicationContext());

        control_back_image.pause();

        AccelerateDecelerateInterpolator a = new AccelerateDecelerateInterpolator();
        RandomTransitionGenerator r = new RandomTransitionGenerator(20000, a);
        control_back_image.setTransitionGenerator(r);


        lastSongId = manage.get_s_Info(getString(R.string.ID));
        lastShuffle = manage.get_b_Info(getString(R.string.Shuffle));
        boolean start = manage.get_b_Info(getString(R.string.Started));
        String position = manage.get_s_Info("position");

        if (manage.get_b_Info(getString(R.string.Songs))) {
            songPosn = Integer.parseInt(position);
            songList = tinydb.getListObject(getString(R.string.Songs), Song.class);
            setStartingList(songList);
            checkInFav(songList.get(songPosn));
        }

        started = start;
        if (lastSongId != null) {
            songPosn = Integer.parseInt(lastSongId);
        }
        if (lastShuffle) {
            shuffle.setImageResource(R.drawable.ic_shuffle);
        } else {
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

        songInfo.setOnClickListener(this);

        playlist.setOnClickListener(this);

        favorite.setOnClickListener(this);

        new checkUpdate().execute();

        adContainerView = findViewById(R.id.banner_container_player);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.mainActId));
        adContainerView.addView(adView);
        loadBanner();
    }

    private void loadBanner() {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder().build();

        AdSize adSize = getAdSize();
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);

        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    public void setPlayingSong(Song song) {
        playingSong = song;
    }

    public Song getPlayingSong() {
        return playingSong;
    }

    public void addNextSong(Song song) {
        songList.add(songPosn + 1, song);
    }

    public void addToQueue(Song song) {
        songList.add(song);
    }

    public void setServiceList() {
        musicSrv.setList(songList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == ALBUM_SONGS || requestCode == ARTIST_SONGS ||
                    requestCode == RECENT_ADDED ||
                    requestCode == SEARCH_RESULT || requestCode == PLAYLIST) {
                int position = data.getIntExtra("position", -1);
                ArrayList<Song> songsArrayList = data.getParcelableArrayListExtra("songList");
                OnFragmentItemClick(position, songsArrayList);
            }
            if (requestCode == NOW_PLAYING) {
                int position = data.getIntExtra("position", -1);
                OnFragmentItemClick(position, songList);
            }
        }
    }

    @Override
    public void OnFragmentItemClick(int position, ArrayList<Song> songsArrayList) {
        songList = songsArrayList;
        fromlist = true;
        songPosn = position;
        musicSrv.setList(songList);
        musicSrv.setSong(songPosn);
        musicSrv.playSong();
        control_back_image.resume();
        if (playbackPaused) {
            playbackPaused = false;
        }
        musicBound = true;
        played = true;
        started = true;
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        songId = String.valueOf(songPosn);
        songName = songList.get(songPosn).getTitle();
        songArtist = songList.get(songPosn).getArtist();
        title.setText(songName);
        artist.setText(songArtist);
        glide_images(songPosn);
        checkInFav(songList.get(songPosn));
    }

    private void songDetails(int pos, ArrayList<Song> list) {
        View v = li.inflate(R.layout.info, null);
        TextView title, artist, album, composer, duration, location;
        title = v.findViewById(R.id.info_title);
        artist = v.findViewById(R.id.info_artist);
        album = v.findViewById(R.id.info_album);
        composer = v.findViewById(R.id.info_composer);
        duration = v.findViewById(R.id.info_duration);
        location = v.findViewById(R.id.info_location);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(v);

        title.setText(list.get(pos).getTitle());
        artist.setText(list.get(pos).getArtist());
        album.setText(list.get(pos).getAlbum());
        composer.setText(list.get(pos).getComposer());
        duration.setText((String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(list.get(pos).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(list.get(pos).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(list.get(pos).getDuration())))));
        location.setText(list.get(pos).getData());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation_2;
        dialog.show();
    }

    public void btnplaypause() {
        if (fromlist) {
            if (musicBound) {
                musicBound = false;
                pause();

            } else {
                musicBound = true;
                start();
                played = true;
            }
        } else {
            if (musicBound) {
                musicBound = false;
                if (!fromButton) {
                    musicSrv.playSong();
                    fromButton = true;
                }
                start();
                played = true;

            } else {
                musicBound = true;
                pause();

            }
        }
    }

    @Override
    public void onClick(View v) {
        if (started) {

            if (v == songInfo) {
                songDetails(songPosn, songList);
            }

            if (v == playPauseSlide || v == playpause) {
                btnplaypause();
            }

            if (v == next) {
                playNext();
            }

            if (v == prev) {
                playPrev();
            }

            if (v == shuffle) {
                musicSrv.setShuffle();
                if (musicSrv.shuffle) {
                    Toast.makeText(this, getString(R.string.Shuffle_On), Toast.LENGTH_SHORT).show();
                    shuffle.setImageResource(R.drawable.ic_shuffle);
                    musicSrv.player.setLooping(false);
                    musicSrv.repeat = false;
                    repeat.setImageResource(R.drawable.ic_repeat_off);
                } else {
                    Toast.makeText(this, getString(R.string.Shuffle_Off), Toast.LENGTH_SHORT).show();
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                }
            }

            if (v == repeat) {
                musicSrv.setRepeat();
                if (musicSrv.repeat) {
                    Toast.makeText(this, getString(R.string.Repeat_On), Toast.LENGTH_SHORT).show();
                    repeat.setImageResource(R.drawable.ic_repeat);
                    musicSrv.shuffle = false;
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                } else {
                    Toast.makeText(this, getString(R.string.Repeat_Off), Toast.LENGTH_SHORT).show();
                    repeat.setImageResource(R.drawable.ic_repeat_off);
                }
            }

            if (v == playlist) {
                Intent i = new Intent(MainActivity.this, NowPlayingActivity.class);
                i.putExtra("songPos", songPosn);
                startActivityForResult(i, NOW_PLAYING);
            }

            if (v == favorite) {
                addToFavPlaylist();
            }

        } else {
            Toast.makeText(this, getString(R.string.playAsong), Toast.LENGTH_SHORT).show();
        }

        if (v == slidePanelTop) {
            if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            } else if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        }
    }

    private void addToFavPlaylist() {
        ArrayList<Song> list = tinydb.getListObject(getString(R.string.favTracks), Song.class);

        if (list.contains(songList.get(songPosn))) {
            list.remove(songList.get(songPosn));
            tinydb.putListObject(getString(R.string.favTracks), list);
        } else {
            list.add(songList.get(songPosn));
            tinydb.putListObject(getString(R.string.favTracks), list);
            Toast.makeText(MainActivity.this, getString(R.string.added_To_Favorite), Toast.LENGTH_SHORT).show();
        }

        checkInFav(songList.get(songPosn));
    }

    public void checkInFav(Song song) {
        ArrayList<Song> list = tinydb.getListObject(getString(R.string.favTracks), Song.class);
        if (list.contains(song)) {
            favorite.setImageResource(R.drawable.ic_fav);
        } else {
            favorite.setImageResource(R.drawable.ic_fav_not);
        }
    }


    private SlidingUpPanelLayout.PanelSlideListener onSlideListener() {
        return new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (started) {
                    if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        title.setSelected(false);
                        artist.setSelected(false);
                        playPauseSlide.setVisibility(View.VISIBLE);
                        player_controller.setVisibility(View.INVISIBLE);
                        RequestOptions requestOptions = new RequestOptions();
                        requestOptions.placeholder(R.drawable.logo);
                        requestOptions.error(R.drawable.logo);
                        if (songList.size() > 0) {
                            Glide.with(getApplicationContext()).setDefaultRequestOptions(requestOptions)
                                    .load(Uri.parse(songList.get(songPosn).getArt())).into(slideImage);
                        } else {
                            slideImage.setImageResource(R.drawable.logo);
                        }
                    } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        player_controller.setVisibility(View.VISIBLE);
                        if (musicSrv.player.isPlaying()) {
                            title.setSelected(true);
                            artist.setSelected(true);
                        } else {
                            title.setSelected(false);
                            artist.setSelected(false);
                        }
                        playPauseSlide.setVisibility(View.GONE);
                        slideImage.setImageResource(R.drawable.logo);
                    }
                }
            }
        };
    }

    public void setNextTitle() {
        songPosn = musicSrv.songPosn;
        glide_images(songPosn);
        title.setText(songList.get(songPosn).getTitle());
        artist.setText(songList.get(songPosn).getArtist());
    }

    public void glide_images(int songPosn) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.logo);
        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(songList.get(songPosn).getArt()))
                .into(control_back_image);
        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(songList.get(songPosn).getArt()))
                .into(cardImage);
        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            Glide.with(getApplicationContext()).setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList.get(songPosn).getArt())).into(slideImage);
        } else {
            playPauseSlide.setVisibility(View.GONE);
            slideImage.setImageResource(R.drawable.logo);
        }
    }

    public void onCallIncoming() {
        playpause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
    }

    public void onCallDisConnected() {
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //menu item selected
        switch (item.getItemId()) {
            case R.id.timer:
                showTimer();
                break;
            case R.id.search:
                Intent i = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(i, SEARCH_RESULT);
                break;
            case R.id.rescan:
                provider = new Provider(this);
                provider.execute();
                break;
            case R.id.share:
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Playback Music Player");
                    String shareMessage = "\nDownload the light weight Playback Music Player app\n\n";
                    shareMessage = shareMessage + "https://play.google.com/store/apps/details?id="
                            + BuildConfig.APPLICATION_ID + "\n\n";
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    //e.toString();
                }
                break;
            case R.id.rateUs:
                launchMarket();
                break;
            case R.id.about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void launchMarket() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(myAppLinkToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Unable to find market app", Toast.LENGTH_LONG).show();
        }
    }

    private void showTimer() {
        final CharSequence[] items = getResources().getStringArray(R.array.timer);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(getString(R.string.choose_Timer));
        dialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        checkedItem = 0;
                        setTimer(0);
                        Toast.makeText(MainActivity.this, getString(R.string.timeOff), Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        checkedItem = 1;
                        setTimer(30 * 60 * 1000);
                        Toast.makeText(MainActivity.this, getString(R.string.mins30), Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        checkedItem = 2;
                        setTimer(60 * 60 * 1000);
                        Toast.makeText(MainActivity.this, getString(R.string.mins60), Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        checkedItem = 3;
                        setTimer(90 * 60 * 1000);
                        Toast.makeText(MainActivity.this, getString(R.string.mins90), Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        checkedItem = 4;
                        setTimer(120 * 60 * 1000);
                        Toast.makeText(MainActivity.this, getString(R.string.mins120), Toast.LENGTH_SHORT).show();
                        break;
                }
                dialog.dismiss();
            }
        });
        dialog.create();
        dialog.show();
    }

    private void setTimer(int time) {
        i = new Intent(this, Timer.class);
        pi = PendingIntent.getBroadcast(this, 1234, i, 0);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (time == 0) {
            am.cancel(pi);
            TIMER = false;
        } else {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + time, pi);
            TIMER = true;
        }
    }

    public void setStartingList(ArrayList<Song> list) {
        songList = list;

        title.setText(songList.get(songPosn).getTitle());
        artist.setText(songList.get(songPosn).getArtist());
        totalDuration.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(songPosn).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(songPosn).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(songPosn).getDuration()))));
        glide_images(songPosn);

    }

    //play next
    public void playNext() {
        if (songList.size() > 0) {
            musicSrv.playNext();
            played = true;
            if (playbackPaused) {
                playbackPaused = false;
            }

            songPosn = musicSrv.songPosn;
            musicBound = fromlist;
            fromButton = true;
            playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
            playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
            glide_images(songPosn);
            title.setText(songList.get(songPosn).getTitle());
            artist.setText(songList.get(songPosn).getArtist());
            checkInFav(songList.get(songPosn));
        } else {
            Toast.makeText(this, "List is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    //play previous
    public void playPrev() {
        if (songList.size() > 0) {
            musicSrv.playPrev();
            played = true;
            if (playbackPaused) {
                playbackPaused = false;
            }
            songPosn = musicSrv.songPosn;
            musicBound = fromlist;
            fromButton = true;
            playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
            playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
            glide_images(songPosn);
            title.setText(songList.get(songPosn).getTitle());
            artist.setText(songList.get(songPosn).getArtist());
            checkInFav(songList.get(songPosn));
        } else {
            Toast.makeText(this, "List is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (slidingLayout != null &&
                (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {

            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            if (musicSrv.isPng()) {
                moveTaskToBack(true);
            } else {
                if (doubleBackToExitPressedOnce) {
                    killApp();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            System.exit(0);
                        }
                    }, 800);
                    return;
                }
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }

        }
    }

    private void killApp() {
        if (played) {
            manage.storeInfo(getString(R.string.ID), String.valueOf(songPosn));
            if (musicSrv != null) {
                manage.storeInfo(getString(R.string.Shuffle), musicSrv.shuffle);
            }
            else{
                manage.storeInfo(getString(R.string.Shuffle), false);
            }
            manage.storeInfo(getString(R.string.Started), started);
            manage.storeInfo(getString(R.string.Songs), true);
            manage.storeInfo("position", String.valueOf(songPosn));
            tinydb.putListObject(getString(R.string.Songs), songList);
        }
        if (TIMER) {
            am.cancel(pi);
        }
        if (musicSrv != null) {
            unbindService(musicConnection);
//        stopService(playIntent);
        }
        musicSrv = null;
        playIntent = null;
        mListeners.clear();
        if (adView != null) {
            adView.destroy();
        }
        finish();
    }

    @Override
    public void onDestroy() {
        killApp();
        super.onDestroy();
    }


    /*
    Media Player Control Methods
    */
    @Override
    public void start() {
        musicSrv.go();
        control_back_image.resume();
        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            title.setSelected(true);
            artist.setSelected(true);
        } else if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            title.setSelected(false);
            artist.setSelected(false);
        }
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        musicSrv.mProgressRunner.run();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        control_back_image.pause();
        title.setSelected(false);
        artist.setSelected(false);
        musicSrv.pausePlayer();
        playpause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
    }

    @Override
    public int getDuration() {
        return musicSrv.getDur();
    }

    @Override
    public int getCurrentPosition() {
        if (musicSrv != null && musicBound && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicSrv != null && musicBound)
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, keyCode + "");
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            //handle click
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                performAction(action);
            }

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void performAction(int action) {
        if (action == KeyEvent.ACTION_DOWN) {
            d++;
            Handler handler = new Handler();
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    // single click *******************************
                    if (d == 1) {
                        btnplaypause();
                        /*Toast.makeText(getApplicationContext(), "single click!", Toast.LENGTH_SHORT).show();*/
                    }
                    // double click *********************************
                    if (d == 2) {
                        playNext();
                        /*Toast.makeText(getApplicationContext(), "Double click!!", Toast.LENGTH_SHORT).show();*/
                    }
                    if (d == 3) {
                        playPrev();
                        /*Toast.makeText(getApplicationContext(), "Triple Click!!", Toast.LENGTH_SHORT).show();*/
                    }
                    d = 0;
                }
            };
            if (d == 1) {
                handler.postDelayed(r, 500);
            }

        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new PlaylistFragment();
                case 1:
                    return new NameWise();
                case 2:
                    return new AlbumsFragment();
                case 3:
                    return new ArtistFragment();
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return null;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.playlist);
                case 1:
                    return getString(R.string.Tracks);
                case 2:
                    return getString(R.string.Albums);
                case 3:
                    return getString(R.string.Artists);
            }
            return null;
        }
    }


    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            //pass alert_list
            if (lastSongId != null) {
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


    //Data change Listeners
    public synchronized void registerDataUpdateListener(DataUpdateListener listener) {
        mListeners.add(listener);
    }

    public synchronized void unregisterDataUpdateListener(DataUpdateListener listener) {
        mListeners.remove(listener);
    }

    public synchronized void dataUpdated() {
        for (DataUpdateListener listener : mListeners) {
            listener.onDataUpdate();
        }
    }


    //Check of app update
    class checkUpdate extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                latestVersion = Jsoup
                        .connect("https://play.google.com/store/apps/details?id="
                                + getPackageName())
                        .timeout(30000)
                        .get()
                        .select("div.hAyfc:nth-child(4)>" +
                                "span:nth-child(2) > div:nth-child(1)" +
                                "> span:nth-child(1)")
                        .first()
                        .ownText();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            currentVersion = BuildConfig.VERSION_NAME;

            if (latestVersion != null) {
                float cVersion = Float.parseFloat(currentVersion);
                float lVersion = Float.parseFloat(latestVersion);

                if (lVersion > cVersion) {
                    updateAlertDialog(lVersion);
                }
            }
        }
    }

    private void updateAlertDialog(float lVersion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.app_name));
        builder.setMessage("New Update Available \nVersion: " + lVersion);
        builder.setCancelable(false);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + getPackageName())));
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Play Store Not Found", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }
}