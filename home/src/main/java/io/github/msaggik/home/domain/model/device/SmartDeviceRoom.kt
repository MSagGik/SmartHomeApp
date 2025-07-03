package io.github.msaggik.home.domain.model.device

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import io.github.msaggik.home.domain.model.device.state.TypeDeviceBluetooth

@Parcelize
data class SmartDeviceRoom (
    val idDevice: Long = 0L,
    val name: String,
    val macAddress: String,
    val typeDevice: TypeDeviceBluetooth,
    val listTypeDeviceRoom: MutableList<TypeDeviceRoom> = mutableListOf(),
    var dateAdd: Long = -1L
): Parcelable
