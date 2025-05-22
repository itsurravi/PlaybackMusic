package com.ravisharma.playbackmusic.data.olddb.prefrences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import com.ravisharma.playbackmusic.data.olddb.model.Song;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;


public class TinyDB {

    private final SharedPreferences preferences;

    public TinyDB(Context appContext) {
        preferences = appContext.getSharedPreferences("DB", Context.MODE_PRIVATE);
    }

    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).commit();
    }


    public ArrayList<String> getListString(String key) {
        return new ArrayList<>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    public void putListObject(String key, ArrayList<Song> objArray) {
        checkForNullKey(key);
        Gson gson = new Gson();
        ArrayList<String> objStrings = new ArrayList<>();
        for (Song obj : objArray) {
            objStrings.add(gson.toJson(obj));
        }
        putListString(key, objStrings);
    }

    public ArrayList<Song> getListObject(String key, Class<Song> mClass) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = getListString(key);
        ArrayList<Song> objects = new ArrayList<>();

        for (String jObjString : objStrings) {
            Song value = gson.fromJson(jObjString, mClass);
            objects.add(value);
        }
        return objects;
    }

    public void removeListObject(String key){
        preferences.edit().remove(key).commit();
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    public void checkForNullKey(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
    }

    public void checkForNullValue(String value) {
        if (value == null) {
            throw new NullPointerException();
        }
    }
}