package com.ravisharma.playbackmusic.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Created by Ravi Sharma on 07-Jan-18.
 */

public class Song implements Parcelable {

    @SerializedName("id")
    private long id;

    @SerializedName("title")
    private String title;

    @SerializedName("artist")
    private String artist;

    @SerializedName("art")
    private String art;

    @SerializedName("duration")
    private long duration;

    @SerializedName("data")
    private String data;

    @SerializedName("dateModified")
    private String dateModified;

    @SerializedName("album")
    private String album;

    @SerializedName("composer")
    private String composer;


    public Song(long songID, String songTitle, String songArtist, String songData, String songDateM, String songArt, long songDur, String songAlbum, String songComposer) {
        id = songID;
        title = songTitle;
        artist = songArtist;
        data = songData;
        dateModified = songDateM;
        art = songArt;
        duration = songDur;
        album = songAlbum;
        composer = songComposer;
    }

    protected Song(Parcel in) {
        id = in.readLong();
        title = in.readString();
        artist = in.readString();
        art = in.readString();
        duration = in.readLong();
        data = in.readString();
        dateModified = in.readString();
        album = in.readString();
        composer = in.readString();
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public long getID() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getData() {
        return data;
    }

    public String getArt() {
        return art;
    }

    public String getDateModified() {
        return dateModified;
    }

    public long getDuration() {
        return duration;
    }

    public String getAlbum() {
        return album;
    }

    public String getComposer() {
        return composer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(art);
        dest.writeLong(duration);
        dest.writeString(data);
        dest.writeString(dateModified);
        dest.writeString(album);
        dest.writeString(composer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return id == song.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
