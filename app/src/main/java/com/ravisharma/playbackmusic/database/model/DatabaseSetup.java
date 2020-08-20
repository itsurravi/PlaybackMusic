package com.ravisharma.playbackmusic.database.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "setupTable")
public class DatabaseSetup {

    @PrimaryKey
    @ColumnInfo(name = "id")
    private long id;

    @ColumnInfo(name = "isSetup")
    boolean isSetup;

    public DatabaseSetup(long id, boolean isSetup) {
        this.id = id;
        this.isSetup = isSetup;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSetup() {
        return isSetup;
    }

    public void setSetup(boolean setup) {
        isSetup = setup;
    }
}
