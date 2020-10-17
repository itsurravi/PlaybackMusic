package com.ravisharma.playbackmusic.prefrences;

import android.content.Context;
import android.content.SharedPreferences;

import com.ravisharma.playbackmusic.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PrefManager {

    SharedPreferences sp;
    SharedPreferences.Editor ed;
    private String Playlist;

    public PrefManager(Context c) {
        sp = c.getSharedPreferences(c.getString(R.string.playback_info), Context.MODE_PRIVATE);
        ed = sp.edit();
        Playlist = c.getString(R.string.playLists);
    }

    public void clearAllData() {
        ed.remove("ID");
        ed.remove("Shuffle");
        ed.remove("RepeatOne");
        ed.remove("Repeat");
        ed.remove("Songs");
        ed.remove("position");
        ed.remove("Started");
        ed.commit();
    }

    public void storeInfo(String key, String data) {
        if (data.equals("remove")) {
            ed.remove(key);
        } else {
            ed.putString(key, data);
        }
        ed.commit();
    }

    public void storeInfo(String key, boolean data) {
        ed.putBoolean(key, data);
        ed.commit();
    }

    public void storeAppVersion(int data) {
        ed.putInt("appVersion", data);
        ed.commit();
    }

    public int getAppVersion() {
        return sp.getInt("appVersion", -1);
    }

    public String get_s_Info(String key) {
        return sp.getString(key, null);
    }

    public boolean get_b_Info(String key) {
        return sp.getBoolean(key, false);
    }

    public void createNewPlaylist(String playlistName) {
        Set<String> list = sp.getStringSet(Playlist, null);
        List<String> l;
        if (list == null) {
            l = new ArrayList<>();
        } else {
            l = new ArrayList<>(list);
        }
        l.add(playlistName);
        Set<String> list2 = new LinkedHashSet<>(l);
        ed.putStringSet(Playlist, list2);
        ed.commit();
    }

    public void renamePlaylist(String oldName, String newName){
        Set<String> list = sp.getStringSet(Playlist, null);
        List<String> l;
        if (list != null) {
            l = new ArrayList<>(list);
            l.set(l.indexOf(oldName), newName);
            Set<String> list2 = new LinkedHashSet<>(l);
            ed.putStringSet(Playlist, list2);
            ed.commit();
        }
    }

    public ArrayList<String> getAllPlaylist() {
        Set<String> list = sp.getStringSet(Playlist, null);
        if (list == null) {
            list = new LinkedHashSet<>();
        }
        return new ArrayList<>(list);
    }

    public void deletePlaylist(String playlistName) {
        Set<String> list = sp.getStringSet(Playlist, null);
        List<String> l = new ArrayList<>(list);
        l.remove(playlistName);

        Set<String> list2 = new LinkedHashSet<>(l);
        ed.putStringSet(Playlist, list2);
        ed.commit();
    }
}
