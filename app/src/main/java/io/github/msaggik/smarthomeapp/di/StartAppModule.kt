package io.github.msaggik.smarthomeapp.di

import io.github.msaggik.smarthomeapp.manager.ActivityManager
import org.koin.dsl.module

val startAppModule = module {
    factory {
        ActivityManager(
            interactor = get()
        )
    }
}