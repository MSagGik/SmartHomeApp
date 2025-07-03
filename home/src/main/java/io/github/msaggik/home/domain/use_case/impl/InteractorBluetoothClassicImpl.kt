package io.github.msaggik.home.domain.use_case.impl

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import io.github.msaggik.home.domain.model.device.LightingUIModel
import io.github.msaggik.home.domain.repository.RepositoryBluetooth
import io.github.msaggik.home.domain.use_case.InteractorBluetoothClassic
import org.json.JSONObject
import java.util.UUID

class InteractorBluetoothClassicImpl(
    private val repositoryBluetooth: RepositoryBluetooth
): InteractorBluetoothClassic {
    override suspend fun connect(uuid: UUID, macAddressDevice: String): Result<Unit> {
        return repositoryBluetooth.connect(
            uuid = uuid,
            macAddressDevice = macAddressDevice
        )
    }

    override fun disconnectDeviceViaMAC(macAddressDevice: String) {
        repositoryBluetooth.disconnectDeviceViaMAC(macAddressDevice)
    }

    override fun disconnectAllDevice() {
        repositoryBluetooth.disconnectAllDevice()
    }

    override fun enablingDataReception(macAddressDevice: String, enabled: Boolean) {
        val jsonLightingUIModelBase = JSONObject().apply {
            put("type", "COM_ENABLED")
            put("state_com", if (enabled) 1 else 0)
        }.toString() + "\n"
        repositoryBluetooth.sendData(
            macAddressDevice = macAddressDevice,
            data = jsonLightingUIModelBase.toByteArray(Charsets.UTF_8)
        )
    }

    override fun sendStartAlphaColorLightingData(lightingUIModel: LightingUIModel) {
        repositoryBluetooth.sendData(
            macAddressDevice = lightingUIModel.macAddress,
            data = LightingUIModel.lightingDataAlphaToByteArrayConvert(lightingUIModel)
        )
    }

    override fun sendStartBaseColorLightingData(lightingUIModel: LightingUIModel) {
        repositoryBluetooth.sendData(
            macAddressDevice = lightingUIModel.macAddress,
            data = LightingUIModel.lightingDataMonoToByteArrayConvert(lightingUIModel)
        )
    }

    override fun sendListColorLightingData(lightingUIModel: LightingUIModel) {
        repositoryBluetooth.sendListData(
            macAddressDevice = lightingUIModel.macAddress,
            listData = LightingUIModel.lightingListDataToByteArrayConvert(lightingUIModel)
        )
    }

    override fun sendStartListColorLightingData(lightingUIModel: LightingUIModel) {
        repositoryBluetooth.sendData(
            macAddressDevice = lightingUIModel.macAddress,
            data = LightingUIModel.lightingStartListDataToByteArrayConvert(lightingUIModel)
        )
    }

    private val receiveDataFlows = mutableMapOf<String, MutableSharedFlow<String>>()

    override fun getDeviceFlow(macAddressDevice: String): SharedFlow<String>? {
        val flow = receiveDataFlows.getOrPut(macAddressDevice) {
            MutableSharedFlow<String>(extraBufferCapacity = 64)
        }
        val hasReceive = repositoryBluetooth.receiveData(macAddressDevice) { data ->
            flow.tryEmit(data)
        }
        return if (hasReceive) {
            flow
        } else {
            receiveDataClear(macAddressDevice)
            null
        }
    }

    override fun receiveDataClear(macAddressDevice: String) {
        repositoryBluetooth.receiveDataClear(macAddressDevice)
        receiveDataFlows.remove(macAddressDevice)
    }

    override fun isConnected(macAddressDevice: String): Pair<Boolean, String?> =
        repositoryBluetooth.isConnected(macAddressDevice)

    override fun getConnectedDevices(): SharedFlow<List<String>> {
        return repositoryBluetooth.getConnectedDevices()
    }

    override fun startDeviceConnectionUpdates() {
        repositoryBluetooth.startDeviceConnectionUpdates()
    }

    override fun stopDeviceConnectionUpdates() {
        repositoryBluetooth.stopDeviceConnectionUpdates()
    }
}