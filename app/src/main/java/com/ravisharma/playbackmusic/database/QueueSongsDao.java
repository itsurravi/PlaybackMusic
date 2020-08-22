package com.ravisharma.playbackmusic.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.ravisharma.playbackmusic.database.model.QueueSongs;

import java.util.List;

@Dao
public interface QueueSongsDao {

    @Insert
    void addSongs(QueueSongs songs);

    @Query("DELETE FROM queueSongs WHERE id=:id")
    void deleteFromQueue(long id);

    @Query("DELETE FROM queueSongs")
    void removeAll();

    @Query("SELECT * FROM queueSongs")
    List<QueueSongs> getQueueSongs();
}
