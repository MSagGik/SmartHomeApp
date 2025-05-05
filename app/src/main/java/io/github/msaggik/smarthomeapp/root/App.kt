package io.github.msaggik.smarthomeapp.root

import android.app.Application
import android.os.StrictMode
import io.github.msaggik.bluetooth.di.dataBluetoothModule
import io.github.msaggik.db.di.dataDbModule
import io.github.msaggik.home.di.homeModule
import io.github.msaggik.settings.di.settingModule
import io.github.msaggik.smarthomeapp.BuildConfig
import io.github.msaggik.smarthomeapp.di.startAppModule
import io.github.msaggik.smarthomeapp.manager.ActivityManager
import io.github.msaggik.sp.di.dataSpModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

internal class App : Application(){

    private val activityManager: ActivityManager by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                startAppModule,
                homeModule,
                settingModule,
                dataSpModule,
                dataDbModule,
                dataBluetoothModule
            )
        }

        if (BuildConfig.DEBUG) {
            initStrictMode()
        }

        activityManager.initSettingApp()
    }

    private fun initStrictMode() {
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .detectActivityLeaks()
                .penaltyDeathOnFileUriExposure()
                .penaltyDeathOnCleartextNetwork()
                .detectFileUriExposure()
                .penaltyLog()
                .penaltyDropBox()
                .build()
        )
    }
}