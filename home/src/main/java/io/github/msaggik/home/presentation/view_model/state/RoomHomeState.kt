package io.github.msaggik.home.presentation.view_model.state

import io.github.msaggik.home.domain.model.room.RoomHome

sealed class RoomHomeState {
    object Loading : RoomHomeState()

    class Content(
        val roomHome: RoomHome
    ) : RoomHomeState()

    class Error(
        val errorMessage: String
    ) : RoomHomeState()
}