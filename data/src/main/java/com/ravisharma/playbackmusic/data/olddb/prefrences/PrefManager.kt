package com.ravisharma.playbackmusic.data.olddb.prefrences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(
    name = "playback_info",
    produceMigrations = ::sharedPreferencesMigration
)

private fun sharedPreferencesMigration(context: Context) =
    listOf(SharedPreferencesMigration(context, "playback_info"))

class PrefManager @Inject constructor(c: Context) {

    private val Playlist: String = "PlayLists"

    private val dataStore = c.dataStore

    fun clearAllData() {
        runBlocking {
            withContext(Dispatchers.Default) {
                dataStore.edit {
                    it.remove(intPreferencesKey("ID"))
                    it.remove(intPreferencesKey("position"))
                    it.remove(booleanPreferencesKey("Shuffle"))
                    it.remove(booleanPreferencesKey("RepeatOne"))
                    it.remove(booleanPreferencesKey("Repeat"))
                    it.remove(booleanPreferencesKey("Songs"))
                    it.remove(booleanPreferencesKey("Started"))
                }
            }
        }
    }

    fun putStringPref(key: String?, data: String) {
        runBlocking {
            withContext(Dispatchers.Default) {
                dataStore.edit {
                    if (data == "remove") {
                        it.remove(stringPreferencesKey(key!!))
                    } else {
                        it[stringPreferencesKey(key!!)] = data
                    }
                }
            }
        }
    }

    fun putBooleanPref(key: String?, data: Boolean) {
        runBlocking {
            withContext(Dispatchers.Default) {
                dataStore.edit {
                    it[booleanPreferencesKey(key!!)] = data
                }
            }
        }
    }

    fun putLongPref(key: String?, data: Long) {
        runBlocking {
            withContext(Dispatchers.Default) {
                dataStore.edit {
                    it[longPreferencesKey(key!!)] = data
                }
            }
        }
    }

    fun storeAppVersion(data: Int) {
        runBlocking {
            withContext(Dispatchers.Default) {
                dataStore.edit {
                    it[intPreferencesKey("appVersion")] = data
                }
            }
        }
    }

    val appVersion: Int
        get() = runBlocking {
            withContext(Dispatchers.Default) {
                dataStore.getValueFlow(intPreferencesKey("appVersion"), -1).first()
            }
        }

    fun getStringPref(key: String?): String {
        return runBlocking { dataStore.getValueFlow(stringPreferencesKey(key!!), "").first() }
    }

    fun getBooleanPref(key: String?): Boolean {
        return runBlocking { dataStore.getValueFlow(booleanPreferencesKey(key!!), false).first() }
    }

    fun getLongPref(key: String?, default: Long): Long {
        return runBlocking { dataStore.getValueFlow(longPreferencesKey(key!!), default).first() }
    }

    suspend fun createNewPlaylist(playlistName: String) {
        val list =
            dataStore.getValueFlow(stringSetPreferencesKey(Playlist), LinkedHashSet())
                .first()
        val l: MutableList<String> = ArrayList(list)
        l.add(playlistName)
        val list2: Set<String> = LinkedHashSet(l)

        dataStore.edit {
            it[stringSetPreferencesKey(Playlist)] = list2
        }
    }

    suspend fun renamePlaylist(oldName: String, newName: String) {
        val list =
            dataStore.getValueFlow(stringSetPreferencesKey(Playlist), LinkedHashSet())
                .first()
        val l: MutableList<String> = ArrayList(list)
        l[l.indexOf(oldName)] = newName
        val list2: Set<String> = LinkedHashSet(l)

        dataStore.edit {
            it[stringSetPreferencesKey(Playlist)] = list2
        }
    }

    fun fetchAllPlayList() =
        dataStore.getValueFlow(
            stringSetPreferencesKey(Playlist),
            LinkedHashSet()
        ).map {
            ArrayList(LinkedHashSet(it))
        }

    suspend fun deletePlaylist(playlistName: String) {
        val list = dataStore.getValueFlow(
            stringSetPreferencesKey(Playlist),
            LinkedHashSet()
        ).first()

        val l: MutableList<String> = ArrayList(list)
        l.remove(playlistName)
        val list2: Set<String> = LinkedHashSet(l)

        dataStore.edit {
            it[stringSetPreferencesKey(Playlist)] = list2
        }
    }
}

fun <T> DataStore<Preferences>.getValueFlow(
    key: Preferences.Key<T>,
    defaultValue: T,
): Flow<T> {
    return this.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[key] ?: defaultValue
        }
}