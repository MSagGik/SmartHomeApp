package io.github.msaggik.home.di

import android.app.Application
import io.github.msaggik.home.data.repositoryimpl.colors.RepositoryDatabaseLightingModeImpl
import io.github.msaggik.home.data.repositoryimpl.devices.RepositoryBluetoothImpl
import io.github.msaggik.home.data.repositoryimpl.rooms.RepositoryDatabaseRoomHomeImpl
import io.github.msaggik.home.domain.repository.RepositoryBluetooth
import io.github.msaggik.home.domain.repository.RepositoryDatabaseLightingMode
import io.github.msaggik.home.domain.repository.RepositoryDatabaseRoomHome
import io.github.msaggik.home.domain.use_case.InteractorBluetoothClassic
import io.github.msaggik.home.domain.use_case.InteractorBluetoothManager
import io.github.msaggik.home.domain.use_case.InteractorDatabaseLightingMode
import io.github.msaggik.home.domain.use_case.InteractorDatabaseRoomHome
import io.github.msaggik.home.domain.use_case.impl.InteractorBluetoothClassicImpl
import io.github.msaggik.home.domain.use_case.impl.InteractorBluetoothManagerImpl
import io.github.msaggik.home.domain.use_case.impl.InteractorDatabaseLightingModeImpl
import io.github.msaggik.home.domain.use_case.impl.InteractorDatabaseRoomHomeImpl
import io.github.msaggik.home.presentation.view_model.HomeViewModel
import io.github.msaggik.home.presentation.view_model.NewLightingModeViewModel
import io.github.msaggik.home.presentation.view_model.NewRoomViewModel
import io.github.msaggik.home.presentation.view_model.RoomHomeViewModel
import io.github.msaggik.home.presentation.view_model.pager_view_model.AutoViewModel
import io.github.msaggik.home.presentation.view_model.pager_view_model.ClimateViewModel
import io.github.msaggik.home.presentation.view_model.pager_view_model.LightingViewModel
import io.github.msaggik.home.presentation.view_model.pager_view_model.TvViewModel
import io.github.msaggik.home.presentation.view_model.pager_view_model.UniversalViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    // view-model
    viewModel{
        AutoViewModel()
    }
    viewModel{
        ClimateViewModel(
            interactorBluetoothClassic = get()
        )
    }
    viewModel{
        LightingViewModel(
            interactorBluetoothClassic = get()
        )
    }
    viewModel{
        TvViewModel()
    }
    viewModel{
        UniversalViewModel()
    }

    viewModel{
        RoomHomeViewModel(
            interactorDatabaseRoomHome = get(),
            interactorDatabaseLightingMode = get(),
            interactorBluetoothClassic = get()
        )
    }

    viewModel{
        HomeViewModel(
            interactorDatabaseRoomHome = get()
        )
    }

    viewModel{
        NewRoomViewModel(
            interactorDatabaseRoomHome = get(),
            interactorBluetoothManager = get()
        )
    }

    viewModel{
        NewLightingModeViewModel(
            application = androidContext() as Application,
            interactorDatabaseLightingMode = get()
        )
    }

    // domain
    single<InteractorDatabaseRoomHome> {
        InteractorDatabaseRoomHomeImpl(
            context = androidContext(),
            repository = get()
        )
    }

    single<InteractorDatabaseLightingMode> {
        InteractorDatabaseLightingModeImpl(
            context = androidContext(),
            repository = get()
        )
    }

    single<InteractorBluetoothManager> {
        InteractorBluetoothManagerImpl(
            repositoryBluetooth = get()
        )
    }

    single<InteractorBluetoothClassic> {
        InteractorBluetoothClassicImpl(
            repositoryBluetooth = get()
        )
    }

    // repository
    single<RepositoryDatabaseRoomHome> {
        RepositoryDatabaseRoomHomeImpl(
            db = get()
        )
    }

    single<RepositoryDatabaseLightingMode> {
        RepositoryDatabaseLightingModeImpl(
            db = get()
        )
    }

    single<RepositoryBluetooth> {
        RepositoryBluetoothImpl(
            context = androidContext(),
            bluetoothConnectManager = get(),
            bluetoothConnectClassic = get()
        )
    }
}