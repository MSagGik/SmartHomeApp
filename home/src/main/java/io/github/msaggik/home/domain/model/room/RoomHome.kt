package io.github.msaggik.home.domain.model.room

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom

@Parcelize
data class RoomHome(
    val idRoomHome: Long = 0L,
    val roomHomeName: String,
    val roomHomeDescription: String,
    val roomHomeImage: String?,
    val listDevice: List<SmartDeviceRoom>,
    var dateModify: Long = -1L
): Parcelable
