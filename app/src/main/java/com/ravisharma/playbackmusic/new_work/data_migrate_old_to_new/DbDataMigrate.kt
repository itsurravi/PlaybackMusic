package com.ravisharma.playbackmusic.new_work.data_migrate_old_to_new

import androidx.annotation.WorkerThread
import com.ravisharma.playbackmusic.database.repository.PlaylistRepository
import com.ravisharma.playbackmusic.new_work.services.data.PlaylistService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DbDataMigrate @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val playlistService: PlaylistService,
) {

    @WorkerThread
    suspend fun migrate() {
        val playLists = playlistRepository.allPlaylistSongs.groupBy {
            it.name
        }.mapValues {
            it.value.map { pl -> pl.song.data }
        }
        playlistService.transferPlaylistToNewDB(playLists)

        playLists.keys.forEach {
            playlistRepository.removePlaylist(it)
        }
    }
}