package io.github.msaggik.home.presentation.view_model.state

import io.github.msaggik.home.domain.model.room.RoomListHome

sealed interface StateRoomListHome {

    object Loading : StateRoomListHome

    data class Content(
        val rooms: RoomListHome
    ) : StateRoomListHome

    object  Error : StateRoomListHome

    object  Empty : StateRoomListHome
}