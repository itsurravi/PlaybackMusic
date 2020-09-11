package com.ravisharma.playbackmusic.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setupTable")
class DatabaseSetup(
        @field:ColumnInfo(name = "id")
        @field:PrimaryKey var id: Long,
        @field:ColumnInfo(name = "isSetup") var isSetup: Boolean
)