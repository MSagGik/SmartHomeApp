package io.github.msaggik.home.data.repositoryimpl.rooms

import io.github.msaggik.db.SmartHomeDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.msaggik.home.data.mappers.HomeMappers
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.home.domain.model.room.RoomListHome
import io.github.msaggik.home.domain.repository.RepositoryDatabaseRoomHome

class RepositoryDatabaseRoomHomeImpl(
    private val db: SmartHomeDatabase
) : RepositoryDatabaseRoomHome {

    override suspend fun setRoomHomeWithDevices(roomHome: RoomHome): Long {
        val (roomHomeEntity, listDevicesEntity) = HomeMappers.map(roomHome = roomHome)
        return db.roomsHomeDao().insertRoomHomeAndAddDevicesInRoomHome(
            roomHome = roomHomeEntity,
            listDevices = listDevicesEntity
        )
    }

    override fun getAllRoomsHomeWithDevices(): Flow<RoomListHome> = flow {
        emit(
            HomeMappers.map(list = db.roomsHomeDao().listRoomsHomeWithDevices())
        )
    }

    override fun getRoomHomeWithDevices(roomHomeId: Long): Flow<RoomHome> = flow {
        emit(
            HomeMappers.map(
                roomHomeWithDevicesEntity = db.roomsHomeDao()
                    .roomHomeWithDevices(roomHomeId = roomHomeId)
            )
        )
    }

    override suspend fun removeRoomHome(roomHomeId: Long): String {
        return db.roomsHomeDao().removeRoomHome(roomHomeId)
    }

    override suspend fun deleteAllRoomsHome(): Pair<Int, List<String>> {
        return db.roomsHomeDao().deleteRoomsHomeAll()
    }
}