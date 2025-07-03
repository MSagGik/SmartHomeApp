package io.github.msaggik.settings.domain.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class TypeDeviceBluetooth(val imageDevice: Int) : Parcelable {
    @Parcelize
    data object Phone : TypeDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_phone_device)
    @Parcelize
    data object Computer : TypeDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_computer_device)
    @Parcelize
    data object AudioAndVideo : TypeDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_audio_and_video_device)
    @Parcelize
    data object Unknown : TypeDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_unknown_device)
}