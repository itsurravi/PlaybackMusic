package com.ravisharma.playbackmusic.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ravisharma.playbackmusic.database.model.ShuffleSongs;

import java.util.List;

@Dao
public interface ShuffleSongsDao {

    @Insert
    void addSong(ShuffleSongs songs);

    @Query("DELETE FROM shuffledSongs WHERE id=:id")
    void deleteFromShuffle(long id);

    @Query("DELETE FROM shuffledSongs")
    void removeAll();

    @Query("SELECT * FROM shuffledSongs")
    List<ShuffleSongs> getShuffleSongs();
}
