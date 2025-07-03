package io.github.msaggik.db.di

import androidx.room.Room
import io.github.msaggik.db.SmartHomeDatabase
import io.github.msaggik.db.entity.config.DatabaseConfig.DATABASE_NAME
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataDbModule = module {
    // db
    single {
        Room.databaseBuilder(
            androidContext(),
            SmartHomeDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    single {
        get<SmartHomeDatabase>().roomsHomeDao()
    }

    single {
        get<SmartHomeDatabase>().lightingModeHomeDao()
    }
}