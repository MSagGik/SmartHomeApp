package io.github.msaggik.smarthomeapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import io.github.msaggik.bluetooth.permission.permissionBluetoothUtil
import io.github.msaggik.smarthomeapp.R
import io.github.msaggik.smarthomeapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding ?: throw IllegalStateException("Binding should not be called after onDestroy")

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private var navControllerMain: NavController? = null
    private var destinationChangedListener: NavController.OnDestinationChangedListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.root_fragments) as? NavHostFragment
        navControllerMain = navHostFragment?.navController

        navControllerMain?.let {
            binding.bottomNavigationMenu.setupWithNavController(it)
        }

        destinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                io.github.msaggik.home.R.id.newLightingModeFragment,
                io.github.msaggik.home.R.id.newRoomFragment,
                io.github.msaggik.home.R.id.roomHomeFragment,
                io.github.msaggik.settings.R.id.devicesFragment -> {
                    bottomNavigationMenuVisible(false)
                }
                else -> {
                    bottomNavigationMenuVisible(true)
                }
            }
        }

        destinationChangedListener?.let { navControllerMain?.addOnDestinationChangedListener(it) }

        permissionBluetooth()
    }

    private fun bottomNavigationMenuVisible(isVisible: Boolean) {
        if (isVisible) {
            binding.bottomNavigationMenu.visibility = View.VISIBLE
        } else {
            binding.bottomNavigationMenu.visibility = View.GONE
        }
    }


    private fun registerPermission() {
        permissionLauncher = this.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (!result.containsValue(true)) {
                Toast.makeText(this, getString(io.github.msaggik.ui.R.string.off_permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun permissionBluetooth() : Boolean {
        if(!::permissionLauncher.isInitialized) {
            registerPermission()
        }
        return applicationContext.permissionBluetoothUtil(permissionLauncher, isRequestPermission = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        removeDestinationChangedListener()
        navControllerMain = null
        _binding = null
    }

    private fun removeDestinationChangedListener() {
        destinationChangedListener?.let {
            navControllerMain?.removeOnDestinationChangedListener(it)
        }
        destinationChangedListener = null
    }
}