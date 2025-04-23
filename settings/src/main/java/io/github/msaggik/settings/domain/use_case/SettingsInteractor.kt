package io.github.msaggik.settings.domain.use_case

import io.github.msaggik.settings.domain.model.LanguageApp
import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.settings.domain.model.ThemeApp
import kotlinx.coroutines.flow.Flow

interface SettingsInteractor {

    suspend fun getTheme() : ThemeApp
    suspend fun updateTheme(theme: ThemeApp)

    suspend fun getLanguageSharedPreferences() : LanguageApp
    suspend fun setLanguage(languageAdd: LanguageApp)

    // manager bluetooth devices
    fun getBluetoothDevices(): Flow<Pair<List<SmartDevice>?, String?>>
    fun searchBluetoothDevices(startSearchState: () -> Unit, finishSearchState: () -> Unit, errorSearchState: (String) -> Unit)
    fun clearSearchBluetoothDevices()
    // service connected
    suspend fun connect(macAddress: String): Result<Unit>
    fun disconnect()
    fun isConnected(): Pair<Boolean, String?>
    fun sendData(data: ByteArray)
    fun receiveData(): Flow<String>
    fun observeConnectedDevice(): Flow<String>
}