package com.msaggik.data.repository_impl.bluetooth

import android.content.Context
import com.msaggik.common_util.Resource
import com.msaggik.common_ui.R
import com.msaggik.data.api.bluetooth.BluetoothDevices
import com.msaggik.data.api.bluetooth.entity.BluetoothSmartDeviceList
import com.msaggik.settings.domain.model.SmartDevice
import com.msaggik.settings.domain.repository.BluetoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class BluetoothRepositoryImpl (
    private val context: Context,
    private val devices: BluetoothDevices
) : BluetoothRepository {

    override fun getBluetoothDevices(): Flow<Resource<List<SmartDevice>>> = flow {
        val response = devices.getBluetoothDevices()
        when (response.resultCode) {
            401 -> {
                emit(Resource.Error(context.getString(R.string.no_check_bluetooth)))
            }
            402 -> {
                emit(Resource.Error(context.getString(R.string.no_permission)))
            }

            200 -> {
                emit(Resource.Success((response as BluetoothSmartDeviceList).list.map {
                    with(it) {
                        SmartDevice(
                            name = name,
                            macAddress = macAddress,
                            checked = checked
                        )
                    }
                }))
            }
        }
    }
}