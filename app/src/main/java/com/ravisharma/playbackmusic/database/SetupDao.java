package com.ravisharma.playbackmusic.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.ravisharma.playbackmusic.database.model.DatabaseSetup;

@Dao
public interface SetupDao {

    @Insert
    void setUp(DatabaseSetup setup);


    @Query("SELECT * FROM setupTable")
    LiveData<DatabaseSetup> dataSetup();
}
