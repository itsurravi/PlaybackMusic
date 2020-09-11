package com.ravisharma.playbackmusic.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ravisharma.playbackmusic.database.model.DatabaseSetup

@Dao
interface SetupDao {
    @Insert
    fun setUp(setup: DatabaseSetup)

    @Query("SELECT * FROM setupTable")
    fun dataSetup(): LiveData<DatabaseSetup>
}