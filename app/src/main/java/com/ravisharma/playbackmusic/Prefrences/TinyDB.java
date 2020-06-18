package com.ravisharma.playbackmusic.Prefrences;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;

import com.ravisharma.playbackmusic.Model.Song;
import com.google.gson.Gson;
import com.ravisharma.playbackmusic.R;

import java.util.ArrayList;
import java.util.Arrays;


public class TinyDB {

    private SharedPreferences preferences;
    private Context context;

    public TinyDB(Context appContext) {
        preferences = appContext.getSharedPreferences(appContext.getString(R.string.DB), Context.MODE_PRIVATE);
        context = appContext;
    }


    /**
     * Put ArrayList of String into SharedPreferences with 'key' and save
     *
     * @param key        SharedPreferences key
     * @param stringList ArrayList of String to be added
     */
    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).commit();
    }


    public ArrayList<String> getListString(String key) {
        return new ArrayList<String>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }

    public void putListObject(String key, ArrayList<Song> objArray) {
        checkForNullKey(key);
        Gson gson = new Gson();
        ArrayList<String> objStrings = new ArrayList<String>();
        for (Song obj : objArray) {
            objStrings.add(gson.toJson(obj));
        }
        putListString(key, objStrings);
    }

    public ArrayList<Song> getListObject(String key, Class<Song> mClass) {
        Gson gson = new Gson();

        ArrayList<String> objStrings = getListString(key);
        ArrayList<Song> objects = new ArrayList<Song>();

        for (String jObjString : objStrings) {
            Song value = gson.fromJson(jObjString, mClass);
            objects.add(value);
        }
        return objects;
    }

    public void removeListObject(String key){
        preferences.edit().remove(key).commit();
    }


    /**
     * Check if external storage is writable or not
     *
     * @return true if writable, false otherwise
     */
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * Check if external storage is readable or not
     *
     * @return true if readable, false otherwise
     */
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