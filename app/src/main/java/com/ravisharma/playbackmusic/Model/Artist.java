package com.ravisharma.playbackmusic.Model;

import com.google.gson.annotations.SerializedName;

public class Artist {

    @SerializedName("artistId")
    private long artistId;

    @SerializedName("artistName")
    private String artistName;

    @SerializedName("numberOfAlbums")
    private String numberOfAlbums;

    @SerializedName("numberOfTracks")
    private String numberOfTracks;

    public Artist(long artistId, String artistName, String numberOfAlbums, String numberOfTracks) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.numberOfAlbums = numberOfAlbums;
        this.numberOfTracks = numberOfTracks;
    }

    public long getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getNumberOfAlbums() {
        return numberOfAlbums;
    }

    public String getNumberOfTracks() {
        return numberOfTracks;
    }
}
