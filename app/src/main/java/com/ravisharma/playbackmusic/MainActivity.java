package com.ravisharma.playbackmusic;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.ravisharma.playbackmusic.activities.AboutActivity;
import com.ravisharma.playbackmusic.activities.EqualizerActivity;
import com.ravisharma.playbackmusic.activities.NowPlayingActivity;
import com.ravisharma.playbackmusic.activities.SearchActivity;
import com.ravisharma.playbackmusic.broadcast.Timer;
import com.ravisharma.playbackmusic.database.PlaylistRepository;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.utils.ads.CustomAdSize;
import com.ravisharma.playbackmusic.fragments.AlbumsFragment;
import com.ravisharma.playbackmusic.fragments.ArtistFragment;
import com.ravisharma.playbackmusic.fragments.NameWise;
import com.ravisharma.playbackmusic.fragments.PlaylistFragment;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.prefrences.PrefManager;
import com.ravisharma.playbackmusic.prefrences.TinyDB;
import com.ravisharma.playbackmusic.provider.Provider;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.flaviofaria.kenburnsview.RandomTransitionGenerator;
import com.ravisharma.playbackmusic.equalizer.EqualizerModel;
import com.ravisharma.playbackmusic.equalizer.EqualizerSettings;
import com.ravisharma.playbackmusic.equalizer.Settings;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Observer;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
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
import java.util.Collections;
import java.util.Iterator;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;

public class MainActivity extends AppCompatActivity implements /*MediaPlayerControl,*/
        View.OnClickListener, NameWise.OnFragmentItemClicked {

    public static final String PREF_KEY = "equalizer";

    private FrameLayout adContainerView;
    private AdView adView;

    private static String TAG;
    String latestVersion, currentVersion;

    public static int d = 0;
    public int sessionId = 0;

    private List<DataUpdateListener> mListeners;

    public final static int ALBUM_SONGS = 1;
    public final static int ARTIST_SONGS = 2;
    public final static int NOW_PLAYING = 3;
    public final static int SEARCH_RESULT = 4;
    public final static int PLAYLIST = 5;
    public final static int RECENT_ADDED = 6;

    public static MainActivity activity;
    public static String songName, songArtist, lastSongId;
    public static boolean lastShuffle = false;
    public static boolean lastRepeat = false;
    public static boolean lastRepeatOne = false;
    public SeekBar seekBar;
    public MusicService musicSrv;

    public TextView title, artist, currentPosition, totalDuration;
    public Toolbar toolbar;
    public KenBurnsView control_back_image;
    public ImageView slideImage, cardImage;
    public ImageView playpause, prev, next, shuffle, repeat, playPauseSlide, eqalizer, playlist, favorite;
    public ViewPager viewPager;
    public SlidingUpPanelLayout slidingLayout;
    public LinearLayout slidePanelTop;
    public RelativeLayout player_controller;
    public Menu menu;
    public LayoutInflater li;

    public boolean musicBound = false, fromlist = false, started = false,
            fromButton = false, played = false, playbackPaused = false, TIMER = false;

    boolean doubleBackToExitPressedOnce = false;

    public ArrayList<Song> songList, normalList;
    public int songPosn;
    private Intent playIntent, i;
    private PendingIntent pi;

    int checkedItem = 0;
    AlarmManager am;

    PrefManager manage;
    //    TinyDB tinydb;
    private PlaylistRepository repository;

    public static Provider provider;

    Song playingSong;

    public Equalizer mEqualizer;
    public BassBoost bassBoost;
    public PresetReverb presetReverb;

    MediaSessionManager mediaSessionManager;
    MediaSession mediaSession;

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
        setSupportActionBar(toolbar);

        getView();
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
        normalList = new ArrayList<Song>();

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
        eqalizer = (ImageView) findViewById(R.id.imgEq);
        playlist = (ImageView) findViewById(R.id.imgPlaylist);
        favorite = (ImageView) findViewById(R.id.imgFav);

        manage = new PrefManager(getApplicationContext());
//        tinydb = new TinyDB(getApplicationContext());
        repository = new PlaylistRepository(this);

        control_back_image.pause();

        AccelerateDecelerateInterpolator a = new AccelerateDecelerateInterpolator();
        RandomTransitionGenerator r = new RandomTransitionGenerator(20000, a);
        control_back_image.setTransitionGenerator(r);


        lastSongId = manage.get_s_Info(getString(R.string.ID));
        lastShuffle = manage.get_b_Info(getString(R.string.Shuffle));
        lastRepeat = manage.get_b_Info(getString(R.string.Repeat));
        lastRepeatOne = manage.get_b_Info(getString(R.string.RepeatOne));
        boolean start = manage.get_b_Info(getString(R.string.Started));
        String position = manage.get_s_Info("position");

        if (manage.get_b_Info(getString(R.string.Songs))) {
//            songList = tinydb.getListObject(getString(R.string.Songs), Song.class);
//            normalList = tinydb.getListObject(getString(R.string.NormalSongs), Song.class);
            songList = repository.getShuffleSongs();
            normalList = repository.getQueueSongs();

            if (songList.size() == 0) {
                start = false;

            } else if (songList.size() <= Integer.parseInt(position)) {
                songPosn = 0;
            } else {
                songPosn = Integer.parseInt(position);
            }
            setStartingList(songList);
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

        if (lastRepeat) {
            if (lastRepeatOne) {
                repeat.setImageResource(R.drawable.ic_repeat_one);
            } else {
                repeat.setImageResource(R.drawable.ic_repeat_all);
            }
        } else {
            repeat.setImageResource(R.drawable.ic_repeat_off);
        }

        repository.getPlaylistSong(getString(R.string.favTracks)).observe(this, new Observer<List<Playlist>>() {
            @Override
            public void onChanged(List<Playlist> playlists) {
                boolean check = false;
                if (started) {
                    for (Playlist playlist : playlists) {
                        Song song = playlist.getSong();
                        check = song.equals(songList.get(songPosn));
                    }
                }
                if (check) {
                    favorite.setImageResource(R.drawable.ic_fav);
                } else {
                    favorite.setImageResource(R.drawable.ic_fav_not);
                }

            }
        });

        slidingLayout.addPanelSlideListener(onSlideListener());

        slidingLayout.getChildAt(1).setOnClickListener(null);

        playPauseSlide.setOnClickListener(this);

        playpause.setOnClickListener(this);

        next.setOnClickListener(this);

        prev.setOnClickListener(this);

        shuffle.setOnClickListener(this);

        repeat.setOnClickListener(this);

        slidePanelTop.setOnClickListener(this);

        eqalizer.setOnClickListener(this);

        playlist.setOnClickListener(this);

        favorite.setOnClickListener(this);

//        new checkUpdate().execute();

        adContainerView = findViewById(R.id.banner_container_player);

        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.mainActId));
        adContainerView.addView(adView);
        loadBanner();

    }

    private void loadBanner() {
        AdRequest adRequest =
                new AdRequest.Builder().build();
        AdSize adSize = CustomAdSize.getAdSize(this);
        adView.setAdSize(adSize);
        adView.loadAd(adRequest);
    }

    public void setPlayingSong(Song song) {
        playingSong = song;
    }

    public Song getPlayingSong() {
        return playingSong;
    }

    public void addNextSong(Song song) {
        if (songList.size() > 0) {
            songList.add(songPosn + 1, song);
        } else {
            Snackbar.make(slidePanelTop, "Play A Song Before Adding", Snackbar.LENGTH_SHORT)
                    .setAnchorView(slidePanelTop).show();
        }
    }

    public void addToQueue(Song song) {
        if (songList.size() > 0) {
            songList.add(song);
        } else {
            Snackbar.make(slidePanelTop, "Play A Song Before Adding", Snackbar.LENGTH_SHORT)
                    .setAnchorView(slidePanelTop).show();
        }
    }

    public void setServiceList() {
        musicSrv.setList(songList);
    }

    private void setPlayerInfo() {
        title.setText(songList.get(songPosn).getTitle());
        artist.setText(songList.get(songPosn).getArtist());
        glide_images(songPosn);
        checkInFav(songList.get(songPosn));
        setPlayingSong(songList.get(songPosn));
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
                OnFragmentItemClick(position, songsArrayList, false);
            }
            if (requestCode == NOW_PLAYING) {
                int position = data.getIntExtra("position", -1);
                OnFragmentItemClick(position, songList, true);
            }
        }
    }

    @Override
    public void OnFragmentItemClick(int position, ArrayList<Song> songsArrayList, boolean nowPlaying) {
        songList = (ArrayList<Song>) songsArrayList.clone();
        fromlist = true;
        songPosn = position;
        if (!nowPlaying) {
            shuffle.setImageResource(R.drawable.ic_shuffle_off);
            musicSrv.shuffle = false;
            repository.saveQueueSongs(normalList);
            repository.saveShuffleSongs(songList);
        }
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

        setPlayerInfo();
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
                Log.d(TAG, "bounded");
                try {
                    if (!fromButton) {
                        musicSrv.playSong();
                        fromButton = true;
                    }
                    start();
                    played = true;
                    musicBound = false;
                } catch (Exception e) {
                    Toast.makeText(this, "Song is deleted or invalid", Toast.LENGTH_SHORT).show();
                }
            } else {
                musicBound = true;
                pause();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (started) {
            if (v == eqalizer) {
                Intent eq = new Intent(MainActivity.this, EqualizerActivity.class);
                startActivity(eq);
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
                    normalList.clear();

                    normalList.addAll(songList);

                    Collections.shuffle(songList);

                    Toast.makeText(this, getString(R.string.Shuffle_On), Toast.LENGTH_SHORT).show();
                    shuffle.setImageResource(R.drawable.ic_shuffle);

                    musicSrv.player.setLooping(false);
                    musicSrv.repeat = false;
                    musicSrv.repeat_one = false;
                    lastRepeatOne = false;
                    lastRepeat = false;
                    repeat.setImageResource(R.drawable.ic_repeat_off);

                    lastShuffle = true;
                } else {
                    if (songList.size() != normalList.size()) {
                        for (Iterator<Song> songIterator = normalList.iterator(); songIterator.hasNext(); ) {
                            Song song = songIterator.next();
                            if (!songList.contains(song)) {
                                songIterator.remove();
                            }
                        }
                    }
                    songList.removeAll(normalList);

                    songList.addAll(normalList);

                    normalList.clear();

                    Log.i("POSITION", songPosn + " " + musicSrv.songPosn);

                    Toast.makeText(this, getString(R.string.Shuffle_Off), Toast.LENGTH_SHORT).show();
                    shuffle.setImageResource(R.drawable.ic_shuffle_off);
                    lastShuffle = false;
                }

                Song playingSong = getPlayingSong();
                Log.d(TAG, getPlayingSong().getTitle());
                int position = songList.indexOf(playingSong);
                songPosn = position;
                musicSrv.setList(songList);
                musicSrv.setSong(position);
                setPlayingSong(songList.get(songPosn));

                repository.saveQueueSongs(normalList);
                repository.saveShuffleSongs(songList);
            }

            if (v == repeat) {
                musicSrv.setRepeat();
                if (musicSrv.repeat && musicSrv.repeat_one) {
                    Toast.makeText(this, getString(R.string.Repeat_One), Toast.LENGTH_SHORT).show();
                    repeat.setImageResource(R.drawable.ic_repeat_one);
                    lastRepeatOne = true;
                    lastRepeat = true;
                } else if (musicSrv.repeat) {
                    lastRepeat = true;
                    lastRepeatOne = false;
                    Toast.makeText(this, getString(R.string.Repeat_On), Toast.LENGTH_SHORT).show();
                    repeat.setImageResource(R.drawable.ic_repeat_all);
                } else {
                    lastRepeatOne = false;
                    lastRepeat = false;
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
        try {
            /*ArrayList<Song> list = tinydb.getListObject(getString(R.string.favTracks), Song.class);

            if (list.contains(songList.get(songPosn))) {
                list.remove(songList.get(songPosn));
                tinydb.putListObject(getString(R.string.favTracks), list);
            } else {
                list.add(songList.get(songPosn));
                tinydb.putListObject(getString(R.string.favTracks), list);
                Toast.makeText(MainActivity.this, getString(R.string.added_To_Favorite), Toast.LENGTH_SHORT).show();
            }

            checkInFav(songList.get(songPosn));*/
            Song song = songList.get(songPosn);
            long exist = repository.isSongExist(getString(R.string.favTracks), song.getId());
            if (exist > 0) {
                repository.removeSong(getString(R.string.favTracks), song.getId());
            } else {
                Playlist playlist = new Playlist(0, getString(R.string.favTracks), song);
                repository.addSong(playlist);
                Toast.makeText(MainActivity.this, getString(R.string.added_To_Favorite), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ignored) {

        }
    }

    public void checkInFav(Song song) {
        /*ArrayList<Song> list = tinydb.getListObject(getString(R.string.favTracks), Song.class);
        if (list.contains(song)) {
            favorite.setImageResource(R.drawable.ic_fav);
        } else {
            favorite.setImageResource(R.drawable.ic_fav_not);
        }*/
        long exist = repository.isSongExist(getString(R.string.favTracks), song.getId());
        if (exist > 0) {
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
        setPlayerInfo();
    }

    public void glide_images(int songPosn) {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.error(R.drawable.logo);
        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(songList.get(songPosn).getArt()))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(control_back_image);
        Glide.with(getApplicationContext())
                .setDefaultRequestOptions(requestOptions)
                .load(Uri.parse(songList.get(songPosn).getArt()))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(cardImage);
        if (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            Glide.with(getApplicationContext()).setDefaultRequestOptions(requestOptions)
                    .load(Uri.parse(songList.get(songPosn).getArt())).into(slideImage);
        } else {
            playPauseSlide.setVisibility(View.GONE);
            slideImage.setImageResource(R.drawable.logo);
        }
    }

    public void setPlayIcons() {
        playpause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        musicBound = !musicBound;
    }

    public void setPauseIcons() {
        playpause.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_pause_white_48dp);
        musicBound = !musicBound;
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
            case R.id.equalizer:
                Intent eq = new Intent(MainActivity.this, EqualizerActivity.class);
                startActivity(eq);
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
            case R.id.suggestion:
                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"sharmaravi.23960@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "Playback Music Player Suggestion");
                email.putExtra(Intent.EXTRA_TEXT, "");
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, "Choose an Email client :"));
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

        setPlayingSong(songList.get(songPosn));
        setPlayerInfo();

        totalDuration.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(songPosn).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(songPosn).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(songPosn).getDuration()))));
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

            setPlayerInfo();
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

            setPlayerInfo();
        } else {
            Toast.makeText(this, "List is Empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (slidingLayout != null &&
                (slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        slidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {

            slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            if (musicSrv != null && musicSrv.isPng()) {
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
            manage.storeInfo(getString(R.string.Shuffle), lastShuffle);
            manage.storeInfo(getString(R.string.Repeat), lastRepeat);
            manage.storeInfo(getString(R.string.RepeatOne), lastRepeatOne);
            manage.storeInfo(getString(R.string.Started), started);
            manage.storeInfo(getString(R.string.Songs), true);
            manage.storeInfo("position", String.valueOf(songPosn));
//            tinydb.putListObject(getString(R.string.Songs), songList);
//            tinydb.putListObject(getString(R.string.NormalSongs), normalList);
        }
        if (TIMER) {
            am.cancel(pi);
        }
        if (musicSrv != null) {
            unbindService(musicConnection);
//        stopService(playIntent);
        }

        mediaSession.release();

        saveEqualizerSettings();

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

    public void pause() {
        playbackPaused = true;
        control_back_image.pause();
        title.setSelected(false);
        artist.setSelected(false);
        musicSrv.pausePlayer();
        playpause.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
        playPauseSlide.setImageResource(R.drawable.uamp_ic_play_arrow_white_48dp);
    }


    //Headset button listener
    /*
     * onKeyDown will work when Activity is in Foreground
     * */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
                    if (d == 1) {
                        btnplaypause();
                    }
                    if (d == 2) {
                        playNext();
                    }
                    if (d == 3) {
                        playPrev();
                    }
                    d = 0;
                }
            };
            if (d == 1) {
                handler.postDelayed(r, 500);
            }
        }
    }

    /*
     * This will work when activity is in background
     * */
    private void initMediaSessions() {
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSession(getApplicationContext(), TAG);
        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        PlaybackState state = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY |
                        PlaybackState.ACTION_PLAY_PAUSE |
                        PlaybackState.ACTION_PAUSE |
                        PlaybackState.ACTION_SKIP_TO_NEXT |
                        PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .setState(musicSrv.player.isPlaying() ? PlaybackState.STATE_PAUSED : PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build();
        mediaSession.setPlaybackState(state);

        mediaSession.setCallback(new MediaSession.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                btnplaypause();
            }

            @Override
            public void onPause() {
                super.onPause();
                btnplaypause();
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                playPrev();
            }
        });

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
                musicSrv.checkRepeat(lastRepeat, lastRepeatOne);
                musicSrv.setSong(Integer.parseInt(lastSongId));
            }
            musicSrv.setUIControls(seekBar, currentPosition, totalDuration);
            musicBound = true;

            loadEqualizerSettings();
            initMediaSessions();
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
        Snackbar.make(slidePanelTop, "Media Scan Completed", Snackbar.LENGTH_SHORT)
                .setAnchorView(slidePanelTop).show();
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
                String currentVer = versionStringToLong(currentVersion);
                String latestVer = versionStringToLong(latestVersion);

                int len = 0;
                len = (currentVer.length() > latestVer.length()) ? currentVer.length() - latestVer.length() : latestVer.length() - currentVer.length();

                StringBuilder sb = new StringBuilder();

                for (int i = 0; i < len; i++) {
                    sb.append("0");
                }

                if (currentVer.length() > latestVer.length()) {
                    latestVer += sb.toString();
                } else {
                    currentVer += sb.toString();
                }

                long cVersion = Long.parseLong(currentVer);
                long lVersion = Long.parseLong(latestVer);

                if (lVersion > cVersion) {
                    updateAlertDialog();
                }
            }
        }
    }

    private String versionStringToLong(String version) {
        String[] split = version.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String s : split) {
            sb.append(s);
        }
        return sb.toString();
    }

    private void updateAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Update Available");
        builder.setMessage("\nCurrent Version: " + currentVersion + "\nLatest Version: " + latestVersion + "\n");
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

        builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog ad = builder.create();
        ad.show();
    }

    private void saveEqualizerSettings() {
        if (Settings.equalizerModel != null) {

            EqualizerSettings settings = new EqualizerSettings();
            settings.bassStrength = Settings.equalizerModel.getBassStrength();
            settings.presetPos = Settings.equalizerModel.getPresetPos();
            settings.reverbPreset = Settings.equalizerModel.getReverbPreset();
            settings.seekbarpos = Settings.equalizerModel.getSeekbarpos();
            settings.isEqualizerEnabled = Settings.equalizerModel.isEqualizerEnabled();
            settings.isEqualizerReloaded = Settings.equalizerModel.isEqualizerReloaded();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

            Gson gson = new Gson();
            preferences.edit()
                    .putString(PREF_KEY, gson.toJson(settings))
                    .apply();
        }

        if (mEqualizer != null) {
            mEqualizer.release();
            mEqualizer = null;
        }

        if (bassBoost != null) {
            bassBoost.release();
            bassBoost = null;
        }

        if (presetReverb != null) {
            presetReverb.release();
            presetReverb = null;
        }
    }

    private void loadEqualizerSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Gson gson = new Gson();
        EqualizerSettings settings = gson.fromJson(preferences.getString(PREF_KEY, "{}"), EqualizerSettings.class);

        EqualizerModel model = new EqualizerModel();
        model.setEqualizerEnabled(settings.isEqualizerEnabled);
        model.setEqualizerReloaded(settings.isEqualizerReloaded);
        model.setBassStrength(settings.bassStrength);
        model.setPresetPos(settings.presetPos);
        model.setReverbPreset(settings.reverbPreset);
        model.setSeekbarpos(settings.seekbarpos);

        Settings.isEqualizerEnabled = settings.isEqualizerEnabled;
        Settings.isEqualizerReloaded = settings.isEqualizerReloaded;
        Settings.bassStrength = settings.bassStrength;
        Settings.presetPos = settings.presetPos;
        Settings.reverbPreset = settings.reverbPreset;
        Settings.seekbarpos = settings.seekbarpos;
        Settings.equalizerModel = model;

        if (Settings.equalizerModel == null) {
            Settings.equalizerModel = new EqualizerModel();
            Settings.equalizerModel.setReverbPreset(PresetReverb.PRESET_NONE);
            Settings.equalizerModel.setBassStrength((short) (1000 / 19));
        }

        if (mEqualizer != null) {
            mEqualizer.release();
            mEqualizer = null;
        }
        if (bassBoost != null) {
            bassBoost.release();
            bassBoost = null;
        }
        if (presetReverb != null) {
            presetReverb.release();
            presetReverb = null;
        }

        mEqualizer = new Equalizer(0, sessionId);
        bassBoost = new BassBoost(0, sessionId);
        presetReverb = new PresetReverb(0, sessionId);

        BassBoost.Settings bassBoostSettingTemp = bassBoost.getProperties();
        BassBoost.Settings bassBoostSetting = new BassBoost.Settings(bassBoostSettingTemp.toString());
        bassBoostSetting.strength = Settings.equalizerModel.getBassStrength();
        bassBoost.setProperties(bassBoostSetting);

        presetReverb.setPreset(Settings.equalizerModel.getReverbPreset());

        mEqualizer.setEnabled(Settings.isEqualizerEnabled);
        bassBoost.setEnabled(Settings.isEqualizerEnabled);
        presetReverb.setEnabled(Settings.isEqualizerEnabled);
        try {
            if (Settings.presetPos == 0) {
                for (short bandIdx = 0; bandIdx < mEqualizer.getNumberOfBands(); bandIdx++) {
                    mEqualizer.setBandLevel(bandIdx, (short) Settings.seekbarpos[bandIdx]);
                }
            } else {
                mEqualizer.usePreset((short) Settings.presetPos);
            }
        } catch (Exception e) {
            Settings.presetPos = 0;
            for (short bandIdx = 0; bandIdx < mEqualizer.getNumberOfBands(); bandIdx++) {
                mEqualizer.setBandLevel(bandIdx, (short) Settings.seekbarpos[bandIdx]);
            }
        }
    }
}