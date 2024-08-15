package com.msaggik.data.api.sp.devices

interface DeviceSharedPreferences {
    suspend fun getMacDeviceSharedPreferences() : String
    suspend fun setMacDeviceSharedPreferences(mac : String)
}