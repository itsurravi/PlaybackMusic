package com.ravisharma.playbackmusic.data.olddb.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Song implements Parcelable {

    @SerializedName("id")
    private final long id;

    @SerializedName("title")
    private final String title;

    @SerializedName("artist")
    private final String artist;

    @SerializedName("art")
    private final String art;

    @SerializedName("duration")
    private final long duration;

    @SerializedName("data")
    private final String data;

    @SerializedName("dateModified")
    private final String dateModified;

    @SerializedName("album")
    private final String album;

    @SerializedName("composer")
    private final String composer;


    public Song(long id, String title, String artist, String data, String dateModified, String art, long duration, String album, String composer) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.data = data;
        this.dateModified = dateModified;
        this.art = art;
        this.duration = duration;
        this.album = album;
        this.composer = composer;
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

    public long getId() {
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
