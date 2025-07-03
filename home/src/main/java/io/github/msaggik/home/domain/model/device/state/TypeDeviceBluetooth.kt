package io.github.msaggik.home.domain.model.device.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class TypeDeviceBluetooth(val number: Int, val imageDevice: Int) : Parcelable {
    @Parcelize
    data object Phone : TypeDeviceBluetooth(0, io.github.msaggik.ui.R.drawable.ic_phone_device)
    @Parcelize
    data object Computer : TypeDeviceBluetooth(1, io.github.msaggik.ui.R.drawable.ic_computer_device)
    @Parcelize
    data object AudioAndVideo : TypeDeviceBluetooth(2, io.github.msaggik.ui.R.drawable.ic_audio_and_video_device)
    @Parcelize
    data object Unknown : TypeDeviceBluetooth(3, io.github.msaggik.ui.R.drawable.ic_unknown_device)
}