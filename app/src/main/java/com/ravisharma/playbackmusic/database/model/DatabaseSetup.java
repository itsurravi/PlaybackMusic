package com.ravisharma.playbackmusic.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "setupTable")
public class DatabaseSetup {

    @ColumnInfo(name = "isSetup")
    boolean isSetup;

    public DatabaseSetup(boolean isSetup) {
        this.isSetup = isSetup;
    }

    public boolean isSetup() {
        return isSetup;
    }

    public void setSetup(boolean setup) {
        isSetup = setup;
    }
}
