package io.github.msaggik.home.domain.use_case

import kotlinx.coroutines.flow.SharedFlow
import io.github.msaggik.home.domain.model.device.LightingUIModel
import java.util.UUID

interface InteractorBluetoothClassic {
    suspend fun connect(uuid: UUID, macAddressDevice: String): Result<Unit>
    fun disconnectDeviceViaMAC(macAddressDevice: String)
    fun disconnectAllDevice()

    fun enablingDataReception(macAddressDevice: String, enabled: Boolean)
    fun sendStartAlphaColorLightingData(lightingUIModel: LightingUIModel)
    fun sendStartBaseColorLightingData(lightingUIModel: LightingUIModel)
    fun sendListColorLightingData(lightingUIModel: LightingUIModel)
    fun sendStartListColorLightingData(lightingUIModel: LightingUIModel)
    fun getDeviceFlow(macAddressDevice: String): SharedFlow<String>?
    fun receiveDataClear(macAddressDevice: String)

    fun isConnected(macAddressDevice: String): Pair<Boolean, String?>
    fun getConnectedDevices(): SharedFlow<List<String>>
    fun startDeviceConnectionUpdates()
    fun stopDeviceConnectionUpdates()
}