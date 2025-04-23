package io.github.msaggik.settings.domain.use_case.impl

import io.github.msaggik.settings.domain.model.LanguageApp
import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.settings.domain.model.ThemeApp
import io.github.msaggik.settings.domain.repository.BluetoothRepository
import io.github.msaggik.settings.domain.repository.LanguageRepository
import io.github.msaggik.settings.domain.repository.ThemeRepository
import io.github.msaggik.settings.domain.use_case.SettingsInteractor
import io.github.msaggik.util.toDataAndError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsInteractorImpl (
    private val repositoryTheme: ThemeRepository,
    private val repositoryLanguage: LanguageRepository,
    private val repositoryBluetooth: BluetoothRepository,
) : SettingsInteractor {

    override suspend fun getTheme(): ThemeApp {
        return repositoryTheme.isLightTheme()
    }

    override suspend fun updateTheme(theme: ThemeApp) {
        repositoryTheme.updateTheme(theme)
    }

    override suspend fun getLanguageSharedPreferences(): LanguageApp {
        return repositoryLanguage.getLanguageSharedPreferences()
    }

    override suspend fun setLanguage(languageAdd: LanguageApp) {
        repositoryLanguage.setLanguage(languageAdd)
    }

    // manager bluetooth devices
    override fun getBluetoothDevices(): Flow<Pair<List<SmartDevice>?, String?>> {
        return repositoryBluetooth.getBluetoothDevices().map { resource -> resource.toDataAndError() }
    }

    override fun searchBluetoothDevices(
        startSearchState: () -> Unit,
        finishSearchState: () -> Unit,
        errorSearchState: (String) -> Unit
    ) {
        repositoryBluetooth.searchBluetoothDevices(
            startSearchState = startSearchState,
            finishSearchState = finishSearchState,
            errorSearchState = errorSearchState
        )
    }

    override fun clearSearchBluetoothDevices() {
        repositoryBluetooth.clearSearchBluetoothDevices()
    }

    // service connected
    override suspend fun connect(macAddress: String): Result<Unit> {
        return repositoryBluetooth.connect(macAddress)
    }

    override fun disconnect() {
        repositoryBluetooth.disconnect()
    }

    override fun isConnected(): Pair<Boolean, String?> = repositoryBluetooth.isConnected()

    override fun sendData(data: ByteArray) {
        repositoryBluetooth.sendData(data)
    }

    override fun receiveData(): Flow<String> = repositoryBluetooth.receiveData()

    override fun observeConnectedDevice(): Flow<String> {
        return repositoryBluetooth.observeConnectedDevice()
    }
}