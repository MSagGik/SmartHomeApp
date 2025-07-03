package io.github.msaggik.home.presentation.view_model

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.msaggik.home.domain.model.device.ColorModel
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.home.domain.model.device.LightingModeState
import io.github.msaggik.home.domain.model.device.LightingUIModel
import io.github.msaggik.home.domain.use_case.InteractorBluetoothClassic
import io.github.msaggik.home.domain.use_case.InteractorDatabaseLightingMode
import io.github.msaggik.home.domain.use_case.InteractorDatabaseRoomHome
import io.github.msaggik.home.presentation.view_model.state.LightingModesState
import io.github.msaggik.home.presentation.view_model.state.PlayPauseLightingModeState
import io.github.msaggik.home.presentation.view_model.state.RoomHomeState
import java.util.UUID

private const val TAG = "RoomHomeViewModel"

class RoomHomeViewModel(
    private val interactorDatabaseRoomHome: InteractorDatabaseRoomHome,
    private val interactorDatabaseLightingMode: InteractorDatabaseLightingMode,
    private val interactorBluetoothClassic: InteractorBluetoothClassic,
) : ViewModel() {

    var currentLightingUIModel: LightingUIModel = LightingUIModel.defaultNullLightingUIModel("")
    var bufferColorModel: ColorModel? = null

    private val lightingModeLiveData = MutableLiveData<LightingModeState>(
        LightingModeState.LightingModePolyState(
        LightingModePoly.defaultNullLightingMode()))
    fun getLightingModeLiveData(): LiveData<LightingModeState> = lightingModeLiveData

    fun setLightingModeLiveData(state: LightingModeState) {
        lightingModeLiveData.postValue(state)
    }

    private val playPauseLightingModeLiveData = MutableLiveData<PlayPauseLightingModeState>(
        PlayPauseLightingModeState.PlayOff)
    fun getPlayPauseLightingModeLiveData(): LiveData<PlayPauseLightingModeState> = playPauseLightingModeLiveData

    fun setPlayPauseLightingModeByMode(modeId: Long) {
        if (modeId > 0) {
            setPlayPauseLightingMode(PlayPauseLightingModeState.Pause(modeId))
            val lightingModes = lightingModesLiveData.value
            if (lightingModes is LightingModesState.Content) {
                lightingModes.lightingModePolies.firstOrNull() { it.id == modeId }?.let { lightingMode ->
                    setLightingModeLiveData(LightingModeState.LightingModePolyState(lightingMode))
                }
            }
        }
    }

    fun setPlayPauseLightingMode(state: PlayPauseLightingModeState) {
        playPauseLightingModeLiveData.postValue(state)
    }

    fun setDrivePlayPauseLightingMode() {
        val state = playPauseLightingModeLiveData.value
        Log.d(TAG, "[setDrivePlayPauseLightingMode] state $state")
        when(state) {
            PlayPauseLightingModeState.PlayOff -> {
                currentLightingUIModel.mode = 0L
            }
            is PlayPauseLightingModeState.Play -> {
                setPlayPauseLightingMode(PlayPauseLightingModeState.Pause(state.idLightingMode))
                currentLightingUIModel.mode = state.idLightingMode
            }
            is PlayPauseLightingModeState.Pause -> {
                setPlayPauseLightingMode(PlayPauseLightingModeState.Play(state.idLightingMode))
                currentLightingUIModel.mode = 0L
            }
            null -> {}
        }
        sendStartListColorLightingDataToIotDevice(currentLightingUIModel)
    }

    // db
    private val roomHomeLiveData = MutableLiveData<RoomHomeState>()
    fun getRoomHomeLiveData(): LiveData<RoomHomeState> = roomHomeLiveData

    fun getItemRoomHome(idRoomHome: Long) {
        Log.i(TAG, "[getItemRoomHome] Fetching RoomHome with ID: $idRoomHome")
        viewModelScope.launch(Dispatchers.IO) {
            roomHomeLiveData.postValue(RoomHomeState.Loading)
            try {
            interactorDatabaseRoomHome.getRoomHomeWithDevices(idRoomHome)
                .collect { roomHome ->
                    Log.d(TAG, "[getItemRoomHome] RoomHome fetched successfully: $roomHome")
                    roomHomeLiveData.postValue(RoomHomeState.Content(roomHome))
                }
            } catch (e: Exception) {
                Log.e(TAG, "[getItemRoomHome] Error fetching RoomHome: ${e.localizedMessage}", e)
                roomHomeLiveData.postValue(RoomHomeState.Error(e.message.toString()))
            }
        }
    }

    fun deleteItemRoomHome(idRoomHome: Long) {
        Log.i(TAG, "[deleteItemRoomHome] Deleting RoomHome with ID: $idRoomHome")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                interactorDatabaseRoomHome.removeRoomHome(idRoomHome)
                Log.i(TAG, "[deleteItemRoomHome] RoomHome deleted successfully: $idRoomHome")
            } catch (e: Exception) {
                Log.e(TAG, "[deleteItemRoomHome] Failed to delete RoomHome: ${e.localizedMessage}", e)
            }
        }
    }

    private val lightingModesLiveData = MutableLiveData<LightingModesState>()
    fun getLightingModesLiveData(): LiveData<LightingModesState> = lightingModesLiveData

    fun getLightingModes() {
        viewModelScope.launch(Dispatchers.IO) {
            lightingModesLiveData.postValue(LightingModesState.Loading)
            interactorDatabaseLightingMode.getAllLightingMode()
                .collect { lightingModes ->
                    lightingModesLiveData.postValue(LightingModesState.Content(lightingModes))
                }
        }
    }

    fun deleteLightingModes(lightingModeId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            interactorDatabaseLightingMode.removeLightingMode(lightingModeId)
        }
    }

    // bluetooth
    fun connect(uuid: UUID, macAddress: String) {
        Log.i(TAG, "[connect] Attempting to connect to device: $macAddress with UUID: $uuid")

        viewModelScope.launch {
            try {
                interactorBluetoothClassic.connect(
                    uuid = uuid,
                    macAddressDevice = macAddress
                )
                Log.i(TAG, "[connect] Connection initiated to device: $macAddress")
            } catch (e: Exception) {
                Log.e(TAG, "[connect] Connection failed to device $macAddress: ${e.localizedMessage}", e)
            }
        }
    }

    private fun disconnectAll() {
        Log.i(TAG, "[disconnectAll] Disconnecting all Bluetooth devices")
        interactorBluetoothClassic.disconnectAllDevice()
    }

    fun sendStartAlphaColorLightingDataToIotDevice(lightingUIModel: LightingUIModel) {
        interactorBluetoothClassic.sendStartAlphaColorLightingData(lightingUIModel)
    }

    fun sendStartBaseColorLightingDataToIotDevice(lightingUIModel: LightingUIModel) {
        interactorBluetoothClassic.sendStartBaseColorLightingData(lightingUIModel)
    }

    fun sendListColorLightingDataToIotDevice(lightingUIModel: LightingUIModel) {
        interactorBluetoothClassic.sendListColorLightingData(lightingUIModel)
    }

    private fun sendStartListColorLightingDataToIotDevice(lightingUIModel: LightingUIModel) {
        interactorBluetoothClassic.sendStartListColorLightingData(lightingUIModel)
    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "[onCleared] ViewModel cleared, cleaning up resources")
        disconnectAll()
    }
}