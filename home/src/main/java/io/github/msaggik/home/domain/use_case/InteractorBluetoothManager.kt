package io.github.msaggik.home.domain.use_case

import kotlinx.coroutines.flow.Flow
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom

interface InteractorBluetoothManager {
    fun getBluetoothDevices(): Flow<Pair<List<SmartDeviceRoom>?, String?>>
}