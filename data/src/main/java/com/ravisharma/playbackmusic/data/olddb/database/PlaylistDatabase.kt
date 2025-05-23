package com.ravisharma.playbackmusic.data.olddb.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ravisharma.playbackmusic.data.olddb.database.dao.LastPlayedDao
import com.ravisharma.playbackmusic.data.olddb.database.dao.MostPlayedDao
import com.ravisharma.playbackmusic.data.olddb.database.dao.PlaylistDao
import com.ravisharma.playbackmusic.data.olddb.database.model.LastPlayed
import com.ravisharma.playbackmusic.data.olddb.database.model.MostPlayed
import com.ravisharma.playbackmusic.data.olddb.model.Playlist
import com.ravisharma.playbackmusic.data.olddb.model.Song
import com.ravisharma.playbackmusic.data.olddb.prefrences.PrefManager
import com.ravisharma.playbackmusic.data.olddb.prefrences.TinyDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

@Database(entities = [Playlist::class, MostPlayed::class, LastPlayed::class], version = 3)
abstract class PlaylistDatabase : RoomDatabase() {

    abstract fun playlistDao(): PlaylistDao
    abstract fun lastPlayedDao(): LastPlayedDao
    abstract fun mostPlayedDao(): MostPlayedDao

    companion object {
        @Volatile
        private var instance: PlaylistDatabase? = null
        private var context: Context? = null

        @Synchronized
        fun getInstance(context: Context?): PlaylistDatabase {
            if (instance == null) {
                Companion.context = context
                instance = Room.databaseBuilder(
                    context!!, PlaylistDatabase::class.java, "PlaylistDB"
                ).addCallback(callback).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .allowMainThreadQueries().build()
            }
            return instance as PlaylistDatabase
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS setupTable")
                database.execSQL("DROP TABLE IF EXISTS queueSongs")
                database.execSQL("DROP TABLE IF EXISTS shuffledSongs")

                database.execSQL("CREATE TABLE `playlistTable_tmp` (`playlistId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `playlistName` TEXT NOT NULL, `id` INTEGER NOT NULL, `title` TEXT, `artist` TEXT, `art` TEXT, `duration` INTEGER NOT NULL, `data` TEXT, `dateModified` TEXT, `album` TEXT, `composer` TEXT)")
                database.execSQL("INSERT INTO `playlistTable_tmp` (playlistId, playlistName, id, title, artist, art, duration, data, dateModified, album, composer) SELECT playlistId, playlistName, id, title, artist, art, duration, data, dateModified, album, composer FROM playlistTable")
                database.execSQL("DROP TABLE playlistTable")
                database.execSQL("ALTER TABLE playlistTable_tmp RENAME TO playlistTable")
                database.execSQL("CREATE TABLE IF NOT EXISTS `lastPlayed` (`playedId` INTEGER PRIMARY KEY AUTOINCREMENT, `id` INTEGER NOT NULL, `title` TEXT, `artist` TEXT, `art` TEXT, `duration` INTEGER NOT NULL, `data` TEXT, `dateModified` TEXT, `album` TEXT, `composer` TEXT)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `mostPlayed` (`playedId` INTEGER PRIMARY KEY AUTOINCREMENT, `playedCount` INTEGER NOT NULL, `id` INTEGER NOT NULL, `title` TEXT, `artist` TEXT, `art` TEXT, `duration` INTEGER NOT NULL, `data` TEXT, `dateModified` TEXT, `album` TEXT, `composer` TEXT)")
            }
        }
        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE IF EXISTS setupTable")
                database.execSQL("DROP TABLE IF EXISTS queueSongs")
                database.execSQL("DROP TABLE IF EXISTS shuffledSongs")

                database.execSQL("CREATE TABLE `playlistTable_tmp` (`playlistId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `playlistName` TEXT NOT NULL, `id` INTEGER NOT NULL, `title` TEXT, `artist` TEXT, `art` TEXT, `duration` INTEGER NOT NULL, `data` TEXT, `dateModified` TEXT, `album` TEXT, `composer` TEXT)")
                database.execSQL("INSERT INTO `playlistTable_tmp` (playlistId, playlistName, id, title, artist, art, duration, data, dateModified, album, composer) SELECT playlistId, playlistName, id, title, artist, art, duration, data, dateModified, album, composer FROM playlistTable")
                database.execSQL("DROP TABLE playlistTable")
                database.execSQL("ALTER TABLE playlistTable_tmp RENAME TO playlistTable")
                database.execSQL("CREATE TABLE IF NOT EXISTS `lastPlayed` (`playedId` INTEGER PRIMARY KEY AUTOINCREMENT, `id` INTEGER NOT NULL, `title` TEXT, `artist` TEXT, `art` TEXT, `duration` INTEGER NOT NULL, `data` TEXT, `dateModified` TEXT, `album` TEXT, `composer` TEXT)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `mostPlayed` (`playedId` INTEGER PRIMARY KEY AUTOINCREMENT, `playedCount` INTEGER NOT NULL, `id` INTEGER NOT NULL, `title` TEXT, `artist` TEXT, `art` TEXT, `duration` INTEGER NOT NULL, `data` TEXT, `dateModified` TEXT, `album` TEXT, `composer` TEXT)")
            }
        }

        private val callback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                transferSongsInDB()
            }
        }

        private fun transferSongsInDB() {
            val tinydb = TinyDB(context)
            val p = PrefManager(context!!)
            val fav = "My Favorites"

            val list = ArrayList<String>()

            CoroutineScope(Dispatchers.IO).launch {
                list.add(fav)
                val differ = async {
                    try {
                        p.fetchAllPlayList().first()
                    } catch (e: Exception) {
                        ArrayList<String>()
                    }
                }
                list.addAll(differ.await())
                for (pName in list) {
                    val songList = ArrayList<Song>()
                    songList.addAll(tinydb.getListObject(pName, Song::class.java))
                    for (song in songList) {
                        val playlist = Playlist(0, pName, song)
                        instance!!.playlistDao().addSong(playlist)
                    }
                }
            }
        }
    }
}