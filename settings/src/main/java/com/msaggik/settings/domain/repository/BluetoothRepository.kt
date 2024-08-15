package com.msaggik.settings.domain.repository

import com.msaggik.common_util.Resource
import com.msaggik.settings.domain.model.SmartDevice
import kotlinx.coroutines.flow.Flow

interface BluetoothRepository {
    fun getBluetoothDevices() : Flow<Resource<List<SmartDevice>>>
}