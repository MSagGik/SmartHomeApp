package io.github.msaggik.sp.api.devices

interface DeviceSharedPreferences {
    suspend fun getMacDeviceSharedPreferences() : String
    suspend fun setMacDeviceSharedPreferences(mac : String)
}