package io.github.msaggik.home.domain.use_case.impl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.repository.RepositoryBluetooth
import io.github.msaggik.home.domain.use_case.InteractorBluetoothManager
import io.github.msaggik.util.toDataAndError

class InteractorBluetoothManagerImpl(
    private val repositoryBluetooth: RepositoryBluetooth
): InteractorBluetoothManager {
    override fun getBluetoothDevices(): Flow<Pair<List<SmartDeviceRoom>?, String?>> {
        return repositoryBluetooth.getBluetoothDevices()
            .map { resource -> resource.toDataAndError() }
    }
}