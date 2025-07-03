package io.github.msaggik.bluetooth.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import io.github.msaggik.bluetooth.api.classic_mono.BluetoothConnectClassicMono
import io.github.msaggik.bluetooth.api.classic_mono.impl.BluetoothConnectClassicMonoImpl
import io.github.msaggik.bluetooth.api.classic_poly.BluetoothConnectClassicPoly
import io.github.msaggik.bluetooth.api.classic_poly.impl.BluetoothConnectClassicPolyImpl
import io.github.msaggik.bluetooth.api.manager.BluetoothConnectManager
import io.github.msaggik.bluetooth.api.manager.impl.BluetoothConnectManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataBluetoothModule = module {

    single<BluetoothConnectClassicPoly> {
        BluetoothConnectClassicPolyImpl(
            context = androidContext(),
            bluetoothAdapter = get()
        )
    }

    single<BluetoothConnectClassicMono> {
        BluetoothConnectClassicMonoImpl(
            context = androidContext(),
            bluetoothAdapter = get()
        )
    }

    single<BluetoothConnectManager> {
        BluetoothConnectManagerImpl(
            context = androidContext(),
            bluetoothAdapter = get()
        )
    }

    single<BluetoothAdapter> {
        (androidContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
}