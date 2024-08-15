package com.msaggik.data.api.sp.devices.impl

import android.content.Context
import android.content.SharedPreferences
import com.msaggik.common_ui.R
import com.msaggik.data.api.sp.devices.DeviceSharedPreferences

private const val MAC_DEVICE_KEY = "mac_device_key"
class DeviceSharedPreferencesImpl (
    private val context: Context,
    private val sp: SharedPreferences
) : DeviceSharedPreferences {

    override suspend fun getMacDeviceSharedPreferences(): String {
        return sp.getString(MAC_DEVICE_KEY, context.getString(R.string.default_)).toString()
    }

    override suspend fun setMacDeviceSharedPreferences(mac: String) {
        sp.edit().putString(MAC_DEVICE_KEY, mac).apply()
    }
}