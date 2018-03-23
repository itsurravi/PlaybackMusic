package com.example.ravisharma.musicdemo1;

import android.net.Uri;

/**
 * Created by Ravi Sharma on 07-Jan-18.
 */

public class Song {

    private long id;
    private String title;
    private String artist;
    private Uri art;
    private long duration;
    private String data;
    private String dateModified;

    public Song(long songID, String songTitle, String songArtist, String songData, String songDateM, Uri songArt, long songDur) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        data=songData;
        dateModified=songDateM;
        art=songArt;
        duration=songDur;
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String getData(){return data;}
    public Uri getArt() {return art;}
    public String getDateModified(){return dateModified;}
    public long getDuration(){return duration;}


}
