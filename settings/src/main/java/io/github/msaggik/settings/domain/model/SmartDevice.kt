package io.github.msaggik.settings.domain.model

import android.os.ParcelUuid
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import io.github.msaggik.settings.domain.model.state.BluetoothType
import io.github.msaggik.settings.domain.model.state.BondState
import io.github.msaggik.settings.domain.model.state.StatusDeviceBluetooth
import io.github.msaggik.settings.domain.model.state.TypeDeviceBluetooth

/**
 * @param name название устройства
 * @param macAddress MAC адрес устройства
 * @param typeDeviceBluetooth тип устройства (телефон, наушники, колонка)
 * @param type тип устройства (неизвестный, только классический, только BLE, оба)
 * @param bondState состояние сопряжения устройства (не сопряжено, в процессе сопряжения, сопряжено)
 * @param uuids список id поддерживаемых сервисов
 * @param statusConnected статус сопряжения в реальном времени
 */
@Parcelize
data class SmartDevice (
    val name: String,
    val macAddress: String,
    val typeDeviceBluetooth: TypeDeviceBluetooth,
    val type: BluetoothType,
    val bondState: BondState,
    val uuids: List<ParcelUuid>?,
    var statusConnected: StatusDeviceBluetooth = StatusDeviceBluetooth.Invisible,
    var isReceived: Boolean = false
): Parcelable
