package io.github.msaggik.home.presentation.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.msaggik.home.domain.use_case.InteractorDatabaseRoomHome
import io.github.msaggik.home.presentation.view_model.state.StateRoomListHome

class HomeViewModel(
    private val interactorDatabaseRoomHome: InteractorDatabaseRoomHome
): ViewModel() {
    private val stateRoomListHomeLiveData = MutableLiveData<StateRoomListHome>()
    fun getStateRoomListHomeLiveData(): LiveData<StateRoomListHome> = stateRoomListHomeLiveData

    fun getRoomsHome() {
        stateRoomListHomeLiveData.postValue(StateRoomListHome.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            interactorDatabaseRoomHome.getAllRoomsHomeWithDevices().collect { roomsHome ->
                when {
                    roomsHome.listRoom == null -> {
                        stateRoomListHomeLiveData.postValue(StateRoomListHome.Error)
                    }
                    roomsHome.listRoom.isEmpty() -> {
                        stateRoomListHomeLiveData.postValue(StateRoomListHome.Empty)
                    }
                    else -> {
                        stateRoomListHomeLiveData.postValue(StateRoomListHome.Content(rooms = roomsHome))
                    }
                }

            }
        }
    }

    fun deleteItemRoomHome(idRoomHome: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            interactorDatabaseRoomHome.removeRoomHome(idRoomHome)
        }
    }
}