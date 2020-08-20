package com.ravisharma.playbackmusic.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import com.ravisharma.playbackmusic.MainActivity;
import com.ravisharma.playbackmusic.R;
import com.ravisharma.playbackmusic.database.PlaylistRepository;
import com.ravisharma.playbackmusic.model.Album;
import com.ravisharma.playbackmusic.model.Artist;
import com.ravisharma.playbackmusic.model.Playlist;
import com.ravisharma.playbackmusic.model.Song;
import com.ravisharma.playbackmusic.SplashScreen;
import com.ravisharma.playbackmusic.prefrences.PrefManager;

import java.util.ArrayList;
import java.util.List;


public class Provider extends AsyncTask<Void, Void, Void> {

    Context c;
    ArrayList<Song> songListByName;
    ArrayList<Song> songListByDate;
    ArrayList<Album> albumList;
    ArrayList<Artist> artistList;

    public Provider(Context c) {
        songListByName = new ArrayList<>();
        songListByDate = new ArrayList<>();
        albumList = new ArrayList<>();
        artistList = new ArrayList<>();
        this.c = c;
    }

    public ArrayList<Song> getSongListByDate() {
        return songListByDate;
    }

    public ArrayList<Song> getSongListByName() {
        return songListByName;
    }

    public ArrayList<Album> getAlbumList() {
        return albumList;
    }

    public ArrayList<Artist> getArtistList() {
        return artistList;
    }

    public ArrayList<Song> getSongList(String albumId) {
        ArrayList<Song> songList = new ArrayList<>();
        ContentResolver musicResolver = c.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null,
                MediaStore.Audio.Media.ALBUM_ID + "=" + albumId + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ",
                new String[]{"%Record%"}, MediaStore.Audio.Media.TITLE + " ASC");

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int composerColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int dateModifyColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisComposer = musicCursor.getString(composerColumn);
                String thisPath = musicCursor.getString(pathColumn);
                String thisDateModify = musicCursor.getString(dateModifyColumn);
                long thisDuration = musicCursor.getLong(durationColumn);
                long thisAlbumAid = musicCursor.getLong(albumIdColumn);
                final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                Uri albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);

                songList.add(new Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, String.valueOf(albumArt), thisDuration, thisAlbum, thisComposer));
            }
            while (musicCursor.moveToNext());
        }

        return songList;
    }

    public ArrayList<Song> getSongListByArtist(String artistId) {
        ArrayList<Song> songList = new ArrayList<>();
        ContentResolver musicResolver = c.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null,
                MediaStore.Audio.Media.ARTIST_ID + "=" + artistId + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ",
                new String[]{"%Record%"}, MediaStore.Audio.Media.TITLE + " ASC");

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int composerColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int dateModifyColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisComposer = musicCursor.getString(composerColumn);
                String thisPath = musicCursor.getString(pathColumn);
                String thisDateModify = musicCursor.getString(dateModifyColumn);
                long thisDuration = musicCursor.getLong(durationColumn);
                long thisAlbumAid = musicCursor.getLong(albumIdColumn);
                final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                Uri albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);

                songList.add(new Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, String.valueOf(albumArt), thisDuration, thisAlbum, thisComposer));
            }
            while (musicCursor.moveToNext());
        }

        return songList;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        /*
        song fetch
        */
        songListByName.clear();
        songListByDate.clear();
        albumList.clear();
        artistList.clear();


        ContentResolver musicResolver = c.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null,
                MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ",
                new String[]{
                        "%Record%"}, "upper("+MediaStore.Audio.Media.TITLE + ") ASC");

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int composerColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int dateModifyColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisComposer = musicCursor.getString(composerColumn);
                String thisPath = musicCursor.getString(pathColumn);
                String thisDateModify = musicCursor.getString(dateModifyColumn);
                long thisDuration = musicCursor.getLong(durationColumn);
                long thisAlbumAid = musicCursor.getLong(albumIdColumn);
                final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                Uri albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);

                songListByName.add(new Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, String.valueOf(albumArt), thisDuration, thisAlbum, thisComposer));
            }
            while (musicCursor.moveToNext());
        }

        musicResolver = c.getContentResolver();
        musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        musicCursor = musicResolver.query(musicUri, null,
                MediaStore.Audio.Media.IS_MUSIC + "!=0 AND " + MediaStore.Audio.Media.DATA + " NOT LIKE ? ",
                new String[]{"%Record%"}, MediaStore.Audio.Media.DATE_MODIFIED + " DESC");

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int composerColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.COMPOSER);
            int pathColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int dateModifyColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED);


            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisComposer = musicCursor.getString(composerColumn);
                String thisPath = musicCursor.getString(pathColumn);
                String thisDateModify = musicCursor.getString(dateModifyColumn);
                long thisDuration = musicCursor.getLong(durationColumn);
                long thisAlbumAid = musicCursor.getLong(albumIdColumn);
                final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                Uri albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);

                songListByDate.add(new Song(thisId, thisTitle, thisArtist, thisPath, thisDateModify, String.valueOf(albumArt), thisDuration, thisAlbum, thisComposer));
            }
            while (musicCursor.moveToNext());
        }

        /*
        album fetch
        */

        musicResolver = c.getContentResolver();
        musicUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        String[] albumProjection = {
                MediaStore.Audio.Albums.ALBUM,
                MediaStore.Audio.Albums._ID,
                MediaStore.Audio.Albums.ARTIST,
                MediaStore.Audio.Albums.NUMBER_OF_SONGS
        };

        musicCursor = musicResolver.query(musicUri, albumProjection,
                null, null, MediaStore.Audio.Media.ALBUM + " ASC");

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums._ID);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Albums.ARTIST);
            int numberOfSongs = musicCursor.getColumnIndex(MediaStore.Audio.Albums.NUMBER_OF_SONGS);


            //add albums to list
            do {

                long thisId = musicCursor.getLong(idColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisNumberOfSongs = musicCursor.getString(numberOfSongs);
                Uri albumArt = null;
                try {
                    long thisAlbumAid = musicCursor.getLong(idColumn);
                    final Uri ART_CONTENT = Uri.parse("content://media/external/audio/albumart");
                    albumArt = ContentUris.withAppendedId(ART_CONTENT, thisAlbumAid);

                    int songs = Integer.parseInt(thisNumberOfSongs);
                    if (songs > 0) {
                        albumList.add(new Album(thisId, albumArt, thisAlbum, thisArtist, thisNumberOfSongs));
                    }
                } catch (Exception ignored) {

                }
            }
            while (musicCursor.moveToNext());
        }

        /*
        Artist Fetch
        */

        musicResolver = c.getContentResolver();
        musicUri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

        String[] artistProjection = {
                MediaStore.Audio.Artists._ID,
                MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
                MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
                MediaStore.Audio.Artists.ARTIST_KEY
        };

        musicCursor = musicResolver.query(musicUri, artistProjection,
                null, null, MediaStore.Audio.Media.ARTIST + " ASC");

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            int albumColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            int tracksColumn = musicCursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS);


            //add artists to list
            do {

                long thisId = musicCursor.getLong(idColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisTracks = musicCursor.getString(tracksColumn);

                artistList.add(new Artist(thisId, thisArtist, thisAlbum, thisTracks));
            }
            while (musicCursor.moveToNext());
        }

        checkInPlaylists();
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        updateMainList();
        if (SplashScreen.shown) {
            if (c instanceof SplashScreen) {
                ((SplashScreen) c).runIntent();
            } else {
                ((MainActivity) c).dataUpdated();
            }
        } else {
            ((SplashScreen) c).runIntent();
        }
        SplashScreen.shown = true;

    }

    private void updateMainList() {
        MainActivity.provider.songListByName = songListByName;
        MainActivity.provider.songListByDate = songListByDate;
        MainActivity.provider.albumList = albumList;
        MainActivity.provider.artistList = artistList;

        Log.d("Sizes:", "List 1 " + songListByDate.size());
        Log.d("Sizes:", "List 2 " + songListByName.size());
        Log.d("Sizes:", "List 3 " + albumList.size());
        Log.d("Sizes:", "List 4 " + artistList.size());
    }

    private void checkInPlaylists() {
        /*TinyDB tinydb;
        tinydb = new TinyDB(c);
        PrefManager p = new PrefManager(c);
        ArrayList<String> list = p.getAllPlaylist();
        List<String> playListArrayList = new ArrayList<>();
        playListArrayList.add("My Favorites");
        playListArrayList.add("NormalSongs");
        playListArrayList.add("Songs");
        playListArrayList.addAll(list);

        for (String playListName : playListArrayList) {
            ArrayList<Song> songList = tinydb.getListObject(playListName, Song.class);

            for (Iterator<Song> iterator = songList.iterator(); iterator.hasNext(); ) {
                Song value = iterator.next();
                if (!songListByName.contains(value)) {
                    iterator.remove();
                }
            }
            if (playListName.equals("Songs") && songList.size() == 0) {
                PrefManager manage = new PrefManager(c);
                manage.storeInfo(c.getString(R.string.Songs), false);
            }
            tinydb.putListObject(playListName, songList);
        }*/
        PlaylistRepository repository = new PlaylistRepository(c);

        List<Song> shuffleSongs = repository.getShuffleSongs();
        for(Song s : shuffleSongs){
            if(!songListByName.contains(s)){
                repository.deleteSongFromShuffle(s.getId());
                repository.deleteSongFromQueue(s.getId());
            }
        }

        if(repository.getShuffleSongs().size()==0){
            PrefManager p = new PrefManager(c);
            p.storeInfo(c.getString(R.string.Songs), false);
            p.storeInfo(c.getString(R.string.Started), false);
        }

        List<Playlist> playlists = repository.getAllPlaylistSongs();

        for(Playlist playlist: playlists){
            Song s = playlist.getSong();
            if(!songListByName.contains(s)){
                repository.removeSong(s.getId());
            }
        }
    }
}
