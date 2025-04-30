package io.github.msaggik.home.presentation.view_model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.home.domain.use_case.InteractorBluetoothManager
import io.github.msaggik.home.domain.use_case.InteractorDatabaseRoomHome
import io.github.msaggik.home.presentation.view_model.state.DeviceRoomSavedState
import io.github.msaggik.home.presentation.view_model.state.RoomHomeState

class NewRoomViewModel(
    private val interactorDatabaseRoomHome: InteractorDatabaseRoomHome,
    private val interactorBluetoothManager: InteractorBluetoothManager
): ViewModel() {

    // SavedBluetoothDevice
    val deviceListSaved: MutableList<SmartDeviceRoom> = mutableListOf()

    private val stateSavedBluetoothDeviceLiveData = MutableLiveData<DeviceRoomSavedState>()
    fun getStateSavedBluetoothDeviceLiveData(): LiveData<DeviceRoomSavedState> = stateSavedBluetoothDeviceLiveData

    fun getSavedDevices() {
        stateSavedBluetoothDeviceLiveData.postValue(DeviceRoomSavedState.Loading)
        viewModelScope.launch(Dispatchers.IO) {
            interactorBluetoothManager
                .getBluetoothDevices()
                .collect { pair -> devicesResult(pair.first, pair.second) }
        }
    }

    private fun devicesResult(foundDevices: List<SmartDeviceRoom>?, errorMessage: String?) {
        when {
            errorMessage != null -> {
                stateSavedBluetoothDeviceLiveData.postValue(DeviceRoomSavedState.Error(errorMessage = errorMessage))
            }
            !foundDevices.isNullOrEmpty() -> {
                stateSavedBluetoothDeviceLiveData.postValue(DeviceRoomSavedState.Content(devices = foundDevices))
            }
            else -> {
                stateSavedBluetoothDeviceLiveData.postValue(DeviceRoomSavedState.Empty)
            }
        }
    }

    // added devices
    private val stateAddedBluetoothDevicesLiveData = MutableLiveData<MutableList<SmartDeviceRoom>>()
    fun getStateAddedBluetoothDevicesLiveData(): LiveData<MutableList<SmartDeviceRoom>> = stateAddedBluetoothDevicesLiveData

    fun addBluetoothDeviceInListLiveData(device: SmartDeviceRoom) {
        device.apply { this.dateAdd = System.currentTimeMillis() }

        val listDeviceRoom = getStateAddedBluetoothDevicesLiveData().value

        listDeviceRoom?.let { list ->
            list.removeAll { it.macAddress == device.macAddress }
            list.add(device)
            stateAddedBluetoothDevicesLiveData.postValue(list)
        } ?: run {
            val list = mutableListOf(device)
            stateAddedBluetoothDevicesLiveData.postValue(list)
        }
    }

    fun addAllBluetoothDeviceInListLiveData(listDevice: MutableList<SmartDeviceRoom>) {
        stateAddedBluetoothDevicesLiveData.postValue(listDevice)
    }

    fun deleteBluetoothDeviceFromListLiveData(device: SmartDeviceRoom) {
        val listDeviceRoom = getStateAddedBluetoothDevicesLiveData().value
        listDeviceRoom?.let { list ->
            list.removeAll { it.macAddress == device.macAddress }
            stateAddedBluetoothDevicesLiveData.postValue(list)
        }
    }

    // image uri room
    var imageURI: Uri? = null

    // db
    fun setRoomHomeWithDevices(roomHome: RoomHome) {
        viewModelScope.launch(Dispatchers.IO) {
            interactorDatabaseRoomHome.setRoomHomeWithDevices(roomHome)
        }
    }

    // getRoomHome
    private val roomHomeLiveData = MutableLiveData<RoomHomeState>()
    fun getRoomHomeLiveData(): LiveData<RoomHomeState> = roomHomeLiveData

    fun getItemRoomHome(idRoomHome: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            roomHomeLiveData.postValue(RoomHomeState.Loading)
            interactorDatabaseRoomHome.getRoomHomeWithDevices(idRoomHome)
                .collect { roomHome ->
                    roomHomeLiveData.postValue(RoomHomeState.Content(roomHome))
                }
        }
    }
}