package com.ravisharma.playbackmusic.Model;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

public class Album {

    @SerializedName("albumId")
    private long albumId;

    @SerializedName("albumArt")
    private Uri albumArt;

    @SerializedName("albumName")
    private String albumName;

    @SerializedName("albumArtist")
    private String albumArtist;

    @SerializedName("numberOfSongs")
    private String numberOfSongs;

    public Album(long albumId, Uri albumArt, String albumName, String albumArtist, String numberOfSongs) {
        this.albumId = albumId;
        this.albumArt = albumArt;
        this.albumName = albumName;
        this.albumArtist = albumArtist;
        this.numberOfSongs = numberOfSongs;
    }

    public long getAlbumId() {
        return albumId;
    }

    public Uri getAlbumArt() {
        return albumArt;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public String getNumberOfSongs() {
        return numberOfSongs;
    }
}
