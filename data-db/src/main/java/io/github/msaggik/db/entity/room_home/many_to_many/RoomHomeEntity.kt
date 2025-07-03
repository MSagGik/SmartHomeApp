package io.github.msaggik.db.entity.room_home.many_to_many

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.msaggik.db.entity.config.DatabaseConfig

@Entity(tableName = DatabaseConfig.ROOM_HOME_TABLE)
data class RoomHomeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DatabaseConfig.ROOM_HOME_ID)
    val idRoomHome: Long = 0L,
    @ColumnInfo(name = DatabaseConfig.ROOM_HOME_NAME)
    val roomHomeName: String,
    @ColumnInfo(name = DatabaseConfig.ROOM_HOME_DESCRIPTION)
    val roomHomeDescription: String?,
    @ColumnInfo(name = DatabaseConfig.ROOM_HOME_IMAGE_URI)
    val roomHomeImage: String?,
    @ColumnInfo(name = DatabaseConfig.ROOM_HOME_DATE_CREATE)
    var dateModify: Long
)