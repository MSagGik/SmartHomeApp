package io.github.msaggik.home.domain.use_case.impl

import android.content.Context
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.home.domain.model.room.RoomListHome
import io.github.msaggik.home.domain.repository.RepositoryDatabaseRoomHome
import io.github.msaggik.util.deleteImageFile
import io.github.msaggik.home.domain.use_case.InteractorDatabaseRoomHome

class InteractorDatabaseRoomHomeImpl(
    val context: Context,
    private val repository: RepositoryDatabaseRoomHome
): InteractorDatabaseRoomHome {
    override suspend fun setRoomHomeWithDevices(roomHome: RoomHome): Long {
        return repository.setRoomHomeWithDevices(roomHome)
    }

    override fun getAllRoomsHomeWithDevices(): Flow<RoomListHome> {
        return repository.getAllRoomsHomeWithDevices()
    }

    override fun getRoomHomeWithDevices(roomHomeId: Long): Flow<RoomHome> {
        return repository.getRoomHomeWithDevices(roomHomeId)
    }

    /**
     * removeRoomHome(roomHomeId: Long): String
     * @return String - image URI for subsequent deletion
     */
    override suspend fun removeRoomHome(roomHomeId: Long) {
        val cleanImageUri = repository.removeRoomHome(roomHomeId)
        if (cleanImageUri.isNotEmpty()) {
            context.deleteImageFile(cleanImageUri.toUri())
        }
    }

    /**
     * deleteAllRoomsHome(): Pair<Int, List<String>>
     * @return Pair<Int, List<String>> - a pair of response codes (where -1 is an unsuccessful request) and a list of image links for subsequent deletion
     */
    override suspend fun deleteAllRoomsHome(): Int {
        val pairResponse = repository.deleteAllRoomsHome()
        if (pairResponse.second.isNotEmpty()) {
            for (imageUri in pairResponse.second) {
                context.deleteImageFile(imageUri.toUri())
            }
        }
        return pairResponse.first
    }
}