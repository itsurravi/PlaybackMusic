package com.ravisharma.playbackmusic.data.utils

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.gson.Gson
import com.ravisharma.playbackmusic.data.db.model.tables.Song
import java.util.Arrays
import javax.inject.Inject

class TinyDb(context: Context) {

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences("tinyDb", Context.MODE_PRIVATE)
    }

    private fun putListString(key: String?, stringList: ArrayList<String>) {
        checkForNullKey(key)
        val myStringList = stringList.toTypedArray()
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).commit()
    }

    private fun getListString(key: String?): ArrayList<String> {
        return ArrayList(
            Arrays.asList(
                *TextUtils.split(
                    preferences.getString(key, ""),
                    "‚‗‚"
                )
            )
        )
    }

    fun putListObject(
        key: String?,
        objArray: List<Song>
    ) {
        checkForNullKey(key)
        val gson = Gson()
        val objStrings = ArrayList<String>()
        for (obj in objArray) {
            objStrings.add(gson.toJson(obj))
        }
        putListString(key, objStrings)
    }

    fun getListObject(
        key: String?,
        mClass: Class<Song>?
    ): ArrayList<Song> {
        val gson = Gson()
        val objStrings = getListString(key)
        val objects: ArrayList<Song> = ArrayList()
        for (jObjString in objStrings) {
            val value: Song = gson.fromJson<Song>(jObjString, mClass)
            objects.add(value)
        }
        return objects
    }

    private fun checkForNullKey(key: String?) {
        if (key == null) {
            throw NullPointerException()
        }
    }
}