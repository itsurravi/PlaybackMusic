package com.ravisharma.playbackmusic.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.ravisharma.playbackmusic.model.Song;

@Entity(tableName = "shuffledSongs")
public class ShuffleSongs {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "shuffleId")
    private long id;

    @Embedded
    private Song song;

    public ShuffleSongs(long id, Song song) {
        this.id = id;
        this.song = song;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }
}
