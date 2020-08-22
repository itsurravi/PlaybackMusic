package com.ravisharma.playbackmusic.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.ravisharma.playbackmusic.model.Playlist;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert
    void addSong(Playlist playlist);

    @Query("DELETE FROM playlistTable WHERE id=:songId")
    void removeSong(long songId);

    @Query("DELETE FROM playlistTable WHERE playlistName=:playlistName AND id=:songId")
    void removeSong(String playlistName, long songId);

    @Query("DELETE FROM playlistTable WHERE playlistName=:playlistName")
    void removePlaylist(String playlistName);


    @Query("SELECT * FROM playlistTable")
    List<Playlist> getAllPlaylistSongs();

    @Query("SELECT * FROM playlistTable WHERE playlistName=:playlistName")
    LiveData<List<Playlist>> getPlaylistSong(String playlistName);

    @Query("SELECT * FROM playlistTable WHERE playlistName=:playlistName")
    List<Playlist> getPlaylist(String playlistName);

    @Query("SELECT COUNT(*) FROM playlistTable WHERE playlistName=:playlistName AND id=:songId")
    long isSongExist(String playlistName, long songId);
}
