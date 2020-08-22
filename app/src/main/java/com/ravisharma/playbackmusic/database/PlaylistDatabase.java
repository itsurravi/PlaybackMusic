package com.ravisharma.playbackmusic.database;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.database.model.DatabaseSetup;
import com.ravisharma.playbackmusic.database.model.QueueSongs;
import com.ravisharma.playbackmusic.database.model.ShuffleSongs;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.prefrences.PrefManager;
import com.ravisharma.playbackmusic.prefrences.TinyDB;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

@Database(entities = {Playlist.class, QueueSongs.class, ShuffleSongs.class, DatabaseSetup.class}, version = 1)
public abstract class PlaylistDatabase extends RoomDatabase {

    public abstract PlaylistDao playlistDao();

    public abstract QueueSongsDao queueSongsDao();

    public abstract ShuffleSongsDao shuffleSongsDao();

    public abstract SetupDao setupDao();

    private static PlaylistDatabase instance = null;
    private static Context context;

    public static synchronized PlaylistDatabase getInstance(Context context) {
        if (instance == null) {
            PlaylistDatabase.context = context;
            instance = Room.databaseBuilder(context,
                    PlaylistDatabase.class,
                    "PlaylistDB")
                    .addCallback(callback)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback callback = new Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            transferSongsInDB();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            String queryString = "UPDATE sqlite_sequence SET seq=0 WHERE name='queueSongs'";
            String queryString2 = "UPDATE sqlite_sequence SET seq=0 WHERE name='shuffledSongs'";
            db.execSQL(queryString);
            db.execSQL(queryString2);
        }
    };

    private static void transferSongsInDB() {
        new TransferSongAsync(PlaylistDatabase.context).execute();
    }

    private static class TransferSongAsync extends AsyncTask<Void, Void, Void> {
        TinyDB tinydb;
        PrefManager p;
        String fav;

        TransferSongAsync(Context context) {
            tinydb = new TinyDB(context);
            p = new PrefManager(context);
            fav = context.getString(R.string.favTracks);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ArrayList<String> list = new ArrayList<>();
            list.add(fav);
            list.addAll(p.getAllPlaylist());

            for (String pName : list) {
                ArrayList<Song> songList = new ArrayList<>();

                songList.addAll(tinydb.getListObject(pName, Song.class));

                for (Song song : songList) {
                    Playlist playlist = new Playlist(0, pName, song);

                    instance.playlistDao().addSong(playlist);
                }
            }

            ArrayList<Song> shuffledSongs = new ArrayList<>();
            shuffledSongs.addAll(tinydb.getListObject(context.getString(R.string.Songs), Song.class));
            for (Song song : shuffledSongs) {
                ShuffleSongs s1 = new ShuffleSongs(0, song);
                instance.shuffleSongsDao().addSong(s1);
            }

            ArrayList<Song> normalSongs = new ArrayList<>();
            normalSongs.addAll(tinydb.getListObject(context.getString(R.string.NormalSongs), Song.class));
            for (Song song : normalSongs) {
                QueueSongs s1 = new QueueSongs(0, song);
                instance.queueSongsDao().addSongs(s1);
            }

            DatabaseSetup setup = new DatabaseSetup(1,true);

            instance.setupDao().setUp(setup);

            return null;
        }
    }
}
