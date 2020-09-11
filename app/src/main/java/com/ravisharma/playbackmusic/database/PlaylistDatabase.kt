package com.ravisharma.playbackmusic.database

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ravisharma.playbackmusic.R
import com.ravisharma.playbackmusic.database.dao.PlaylistDao
import com.ravisharma.playbackmusic.database.dao.QueueSongsDao
import com.ravisharma.playbackmusic.database.dao.SetupDao
import com.ravisharma.playbackmusic.database.dao.ShuffleSongsDao
import com.ravisharma.playbackmusic.database.model.DatabaseSetup
import com.ravisharma.playbackmusic.database.model.QueueSongs
import com.ravisharma.playbackmusic.database.model.ShuffleSongs
import com.ravisharma.playbackmusic.model.Playlist
import com.ravisharma.playbackmusic.model.Song
import com.ravisharma.playbackmusic.prefrences.PrefManager
import com.ravisharma.playbackmusic.prefrences.TinyDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [Playlist::class, QueueSongs::class, ShuffleSongs::class, DatabaseSetup::class], version = 1)
abstract class PlaylistDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao
    abstract fun queueSongsDao(): QueueSongsDao
    abstract fun shuffleSongsDao(): ShuffleSongsDao
    abstract fun setupDao(): SetupDao

    companion object {
        private var instance: PlaylistDatabase? = null
        private var context: Context? = null

        @Synchronized
        fun getInstance(context: Context?): PlaylistDatabase {
            if (instance == null) {
                Companion.context = context
                instance = Room.databaseBuilder(context!!,
                        PlaylistDatabase::class.java,
                        "PlaylistDB")
                        .addCallback(callback)
                        .allowMainThreadQueries()
                        .build()
            }
            return instance as PlaylistDatabase
        }

        private val callback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                transferSongsInDB()
            }
        }

        private fun transferSongsInDB() {
            val tinydb = TinyDB(context)
            val p = PrefManager(context)
            val fav = context!!.getString(R.string.favTracks)

            val list = ArrayList<String>()

            CoroutineScope(Dispatchers.IO).launch{
                list.add(fav)
                list.addAll(p.allPlaylist)
                for (pName in list) {
                    val songList = ArrayList<Song>()
                    songList.addAll(tinydb.getListObject(pName, Song::class.java))
                    for (song in songList) {
                        val playlist = Playlist(0, pName, song)
                        instance!!.playlistDao().addSong(playlist)
                    }
                }
                val setup = DatabaseSetup(1, true)
                instance!!.setupDao().setUp(setup)
            }
        }
    }
}