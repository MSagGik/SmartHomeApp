package io.github.msaggik.settings.domain.model.state

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class StatusDeviceBluetooth(val imageStatusDevice: Int) : Parcelable {
    @Parcelize
    data object Connect : StatusDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_bluetooth_connect)
    @Parcelize
    data object Connected : StatusDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_bluetooth_connected)
    @Parcelize
    data object Disconnect : StatusDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_bluetooth_disconnect)
    @Parcelize
    data object Invisible : StatusDeviceBluetooth(io.github.msaggik.ui.R.drawable.ic_empty)
}