package com.ravisharma.playbackmusic.model;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlistTable")
public class Playlist {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "playlistId")
    private long id;

    @ColumnInfo(name = "playlistName")
    private String name;

    @Embedded
    private Song song;

    public Playlist(long id, String name, Song song) {
        this.id = id;
        this.name = name;
        this.song = song;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Song getSong() {
        return song;
    }

    public void setSong(Song song) {
        this.song = song;
    }
}
