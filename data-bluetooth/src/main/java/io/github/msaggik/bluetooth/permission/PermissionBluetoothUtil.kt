package io.github.msaggik.bluetooth.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Utility object for managing Bluetooth permissions across different Android API levels.
 *
 * Handles requesting and verifying Bluetooth-related permissions dynamically
 * based on the device's Android version (pre-S or S+).
 */
object PermissionUtil

@RequiresApi(api = Build.VERSION_CODES.S)
private val permissionNew = arrayOf(
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.BLUETOOTH_SCAN
)

private val permissionOld = arrayOf(
    Manifest.permission.BLUETOOTH,
    Manifest.permission.BLUETOOTH_ADMIN
)

/**
 * Checks whether the required Bluetooth permissions are granted based on the Android version.
 * If not granted and [isRequestPermission] is true, this function will launch a permission request.
 *
 * @receiver The [Context] used to check and request permissions.
 * @param permissionLauncher The launcher for requesting multiple permissions using the Activity Result API.
 * @param isRequestPermission If true, requests the permissions if they are not already granted.
 * @return `true` if all required Bluetooth permissions are already granted; otherwise, `false`.
 *
 * ## Required permissions:
 * - Android 12 (S) and above:
 *   - [Manifest.permission.BLUETOOTH]
 *   - [Manifest.permission.BLUETOOTH_ADMIN]
 *   - [Manifest.permission.BLUETOOTH_CONNECT]
 *   - [Manifest.permission.BLUETOOTH_SCAN]
 * - Below Android 12:
 *   - [Manifest.permission.BLUETOOTH]
 *   - [Manifest.permission.BLUETOOTH_ADMIN]
 *
 * ## Example usage:
 * ```kotlin
 * if (context.permissionBluetoothUtil(permissionLauncher, isRequestPermission = true)) {
 *     // Permissions granted, proceed with Bluetooth logic
 * }
 * ```
 */
fun Context.permissionBluetoothUtil(permissionLauncher: ActivityResultLauncher<Array<String>>, isRequestPermission: Boolean) : Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        permissionNew
    } else {
        permissionOld
    }
    val hasPermission = permission.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
    if (!hasPermission && isRequestPermission) {
        permissionLauncher.launch(permission)
    }
    return hasPermission
}