package io.github.msaggik.home.domain.use_case

import io.github.msaggik.home.domain.model.room.RoomHome
import kotlinx.coroutines.flow.Flow
import io.github.msaggik.home.domain.model.room.RoomListHome

interface InteractorDatabaseRoomHome {
    suspend fun setRoomHomeWithDevices(roomHome: RoomHome): Long
    fun getAllRoomsHomeWithDevices(): Flow<RoomListHome>
    fun getRoomHomeWithDevices(roomHomeId: Long): Flow<RoomHome>
    suspend fun removeRoomHome(roomHomeId: Long)
    suspend fun deleteAllRoomsHome(): Int
}