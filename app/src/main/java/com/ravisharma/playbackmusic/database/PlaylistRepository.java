package com.ravisharma.playbackmusic.database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.ravisharma.playbackmusic.database.model.DatabaseSetup;
import com.ravisharma.playbackmusic.database.model.QueueSongs;
import com.ravisharma.playbackmusic.database.model.ShuffleSongs;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.model.Song;

import java.util.ArrayList;
import java.util.List;

public class PlaylistRepository {

    private SetupDao setupDao;
    private QueueSongsDao queueSongsDao;
    private ShuffleSongsDao shuffleSongsDao;
    private PlaylistDao playlistDao;
    private LiveData<List<Playlist>> playlistSong;
    private PlaylistDatabase database;

    public PlaylistRepository(Context context) {
        database = PlaylistDatabase.getInstance(context);
        setupDao = database.setupDao();
        playlistDao = database.playlistDao();
        queueSongsDao = database.queueSongsDao();
        shuffleSongsDao = database.shuffleSongsDao();
    }

    public LiveData<DatabaseSetup> isDatabaseRead(){
        return setupDao.dataSetup();
    }

    //Playlist Operations
    public LiveData<List<Playlist>> getPlaylistSong(String playlistName) {
        playlistSong = playlistDao.getPlaylistSong(playlistName);
        return playlistSong;
    }

    public List<Playlist> getPlaylist(String playlistName) {
        return playlistDao.getPlaylist(playlistName);
    }

    public List<Playlist> getAllPlaylistSongs() {
        return playlistDao.getAllPlaylistSongs();
    }

    public long isSongExist(String playlistName, long songId) {
        return playlistDao.isSongExist(playlistName, songId);
    }

    public void addSong(Playlist playlist) {
        new AddSong().execute(playlist);
    }

    public void removeSong(long songId) {
        new RemoveSong(null, songId).execute();
    }

    public void removeSong(String playlistName, long songId) {
        new RemoveSong(playlistName, songId).execute();
    }

    public void removePlayist(String playlistName) {
        new RemovePlaylist().execute(playlistName);
    }

    private class AddSong extends AsyncTask<Playlist, Void, Void> {

        @Override
        protected Void doInBackground(Playlist... playlists) {
            playlistDao.addSong(playlists[0]);
            return null;
        }
    }

    private class RemoveSong extends AsyncTask<Void, Void, Void> {

        String playlistName;
        long songId;

        public RemoveSong(String playlistName, long songId) {
            this.playlistName = playlistName;
            this.songId = songId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (playlistName == null) {
                playlistDao.removeSong(songId);
            } else {
                playlistDao.removeSong(playlistName, songId);
            }
            return null;
        }
    }

    private class RemovePlaylist extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... playlist) {
            playlistDao.removePlaylist(playlist[0]);
            return null;
        }
    }


    //QueueSongs Operations
    public void saveQueueSongs(ArrayList<Song> songList) {
        new ClearQueueAsync(songList).execute();
    }

    public ArrayList<Song> getQueueSongs() {
        List<QueueSongs> songs = queueSongsDao.getQueueSongs();
        ArrayList<Song> songList = new ArrayList<>();
        for (QueueSongs song : songs) {
            songList.add(song.getSong());
        }

        return songList;
    }

    public void deleteSongFromQueue(long id) {
        new DeleteSongFromQueueAsync().execute(id);
    }

    private class ClearQueueAsync extends AsyncTask<Void, Void, Void> {

        private ArrayList<Song> songList;

        public ClearQueueAsync(ArrayList<Song> songList) {
            this.songList = songList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            queueSongsDao.removeAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new SaveQueueAsync(songList).execute();
        }
    }

    private class SaveQueueAsync extends AsyncTask<Void, Void, Void> {

        private ArrayList<Song> songList;

        public SaveQueueAsync(ArrayList<Song> songList) {
            this.songList = songList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (Song song : songList) {
                QueueSongs qSong = new QueueSongs(0, song);
                queueSongsDao.addSongs(qSong);
            }
            return null;
        }

    }

    private class DeleteSongFromQueueAsync extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... value) {
            queueSongsDao.deleteFromQueue(value[0]);
            return null;
        }
    }

    //ShuffleSongs Operations
    public void saveShuffleSongs(ArrayList<Song> songList) {
        new ClearShuffleAsync(songList).execute();
    }

    public ArrayList<Song> getShuffleSongs() {
        List<ShuffleSongs> songs = shuffleSongsDao.getShuffleSongs();
        Log.d("SongsList", songs.size() + "");
        ArrayList<Song> songList = new ArrayList<>();
        for (ShuffleSongs song : songs) {
            songList.add(song.getSong());
        }

        return songList;
    }

    public void deleteSongFromShuffle(long id) {
        new DeleteSongFromShuffleAsync().execute(id);
    }

    private class ClearShuffleAsync extends AsyncTask<Void, Void, Void> {
        private ArrayList<Song> songList;

        public ClearShuffleAsync(ArrayList<Song> songList) {
            this.songList = songList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            shuffleSongsDao.removeAll();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            new SaveShuffleAsync(songList).execute();
        }
    }

    private class SaveShuffleAsync extends AsyncTask<Void, Void, Void> {
        private ArrayList<Song> songList;

        public SaveShuffleAsync(ArrayList<Song> songList) {
            this.songList = songList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (Song song : songList) {
                ShuffleSongs qSong = new ShuffleSongs(0, song);
                shuffleSongsDao.addSong(qSong);
            }
            return null;
        }
    }

    private class DeleteSongFromShuffleAsync extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... value) {
            shuffleSongsDao.deleteFromShuffle(value[0]);
            return null;
        }
    }
}
