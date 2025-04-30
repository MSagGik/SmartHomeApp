package io.github.msaggik.home.domain.repository

import kotlinx.coroutines.flow.Flow
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.home.domain.model.room.RoomListHome

interface RepositoryDatabaseRoomHome {
    /**
     * setRoomHomeWithDevices(roomHome: RoomHome): Long
     * @return Long - row id in db table RoomEntity
     */
    suspend fun setRoomHomeWithDevices(roomHome: RoomHome): Long

    fun getAllRoomsHomeWithDevices(): Flow<RoomListHome>

    fun getRoomHomeWithDevices(roomHomeId: Long): Flow<RoomHome>

    /**
     * removeRoomHome(roomHomeId: Long): String
     * @return String - image URI for subsequent deletion
     */
    suspend fun removeRoomHome(roomHomeId: Long): String

    /**
     * deleteAllRoomsHome(): Pair<Int, List<String>>
     * @return Pair<Int, List<String>> - a pair of response codes (where -1 is an unsuccessful request) and a list of image links for subsequent deletion
     */
    suspend fun deleteAllRoomsHome(): Pair<Int, List<String>>
}