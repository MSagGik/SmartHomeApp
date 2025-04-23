package io.github.msaggik.settings.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.msaggik.bluetooth.permission.permissionBluetoothUtil
import io.github.msaggik.settings.R
import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.settings.domain.model.state.StatusDeviceBluetooth
import io.github.msaggik.settings.presentation.ui.adapters.DevicesAdapter
import io.github.msaggik.settings.presentation.view_model.state.DeviceSavedState
import io.github.msaggik.settings.presentation.view_model.DevicesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import io.github.msaggik.settings.data.mapers.SettingMappers
import io.github.msaggik.settings.databinding.FragmentDevicesBinding
import io.github.msaggik.settings.presentation.view_model.state.DeviceSearchedState
import io.github.msaggik.util.DebounceMode
import io.github.msaggik.util.debounce
import io.github.msaggik.util.showAndHideOthers
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val DELAY_CLICK = 500L
private const val RESOURCE_CLOSING_TIME = 1_400L

class DevicesFragment : Fragment() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    private fun registerPermission() {
        permissionLauncher =
            this.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                if (!result.containsValue(true)) {
                    Toast.makeText(
                        requireContext(),
                        getString(io.github.msaggik.ui.R.string.off_permission),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun permissionBluetooth(): Boolean {
        if (!::permissionLauncher.isInitialized) {
            registerPermission()
        }
        return requireContext().applicationContext.permissionBluetoothUtil(
            permissionLauncher,
            isRequestPermission = true
        )
    }

    private val devicesViewModel: DevicesViewModel by viewModel()

    private var _binding: FragmentDevicesBinding? = null
    private val binding: FragmentDevicesBinding get() = _binding ?: throw IllegalStateException("Binding should not be called after onDestroy")
    private var viewArraySaved: Array<View>? = null
    private var viewArraySearched: Array<View>? = null

    private lateinit var searchBluetoothDeviceClickDebounce: (Unit) -> Unit

    private val deviceAdapterSaved: DevicesAdapter by lazy {
        DevicesAdapter(devicesViewModel.deviceListSaved) {
            deviceSelectionSaved(it)
        }
    }

    private fun deviceSelectionSaved(selectedDevice: SmartDevice) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            if (permissionBluetooth()) {
                val (isConnected, connectedMac) = devicesViewModel.isConnected()
                when {
                    !isConnected -> devicesViewModel.connect(selectedDevice.macAddress)
                    selectedDevice.macAddress == connectedMac -> devicesViewModel.disconnect()
                    else -> {
                        devicesViewModel.disconnect()
                        withContext(Dispatchers.IO) {
                            delay(RESOURCE_CLOSING_TIME)
                            withContext(Dispatchers.Main) {
                                devicesViewModel.connect(selectedDevice.macAddress)
                            }
                        }
                    }
                }
            }
        }
    }

    private val deviceAdapterSearched: DevicesAdapter by lazy {
        DevicesAdapter(devicesViewModel.deviceListSearched) {
            deviceSelectionSearched(it)
        }
    }

    private fun deviceSelectionSearched(device: SmartDevice) {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        registerPermission()
        viewArraySaved = arrayOf(
            binding.loadingTimeSaved,
            binding.listBluetoothSaved,
            binding.errorMessageSaved,
            binding.emptyListDevicesSaved
        )
        viewArraySearched = arrayOf(
            binding.loadingTimeSearched,
            binding.listBluetoothSearched,
            binding.errorMessageSearched,
            binding.emptyListDevicesSearched
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.registerReceiver(broadcastReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))

        // saved devices
        binding.listBluetoothSaved.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.listBluetoothSaved.adapter = deviceAdapterSaved

        devicesViewModel.getStateSavedBluetoothDeviceLiveData().observe(viewLifecycleOwner) {
            renderSavedDevice(it)
        }

        // searched devices
        binding.listBluetoothSearched.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.listBluetoothSearched.adapter = deviceAdapterSearched

        if (!devicesViewModel.isConnected().first) {
            devicesViewModel.getSavedDevices()
            searchBluetoothDevices()
        }

        searchBluetoothDeviceClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<Unit>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) { searchBluetoothDevices() }

        devicesViewModel.getStateSearchedBluetoothDeviceLiveData().observe(viewLifecycleOwner) {
            renderSearchedDevice(it)
        }

        // observe connected devices
        lifecycleScope.launch {
            devicesViewModel.observeConnectedDevice.collect { address ->
                renderConnectedDevices(address)
            }
        }

        devicesViewModel.startObserveConnectedDevice()

        // received data
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                devicesViewModel.receivedData.collect { data ->
                    Log.d("Bluetooth", "Received data: $data")
                    Toast.makeText(requireContext(), "message: $data", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonBack.setOnClickListener(listener)
        binding.searchDevice.setOnClickListener(listener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderSavedDevice(state: DeviceSavedState) {
        when (state) {
            is DeviceSavedState.Loading -> binding.loadingTimeSaved.showAndHideOthers(viewArraySaved)
            is DeviceSavedState.Content -> {
                binding.listBluetoothSaved.showAndHideOthers(viewArraySaved)
                devicesViewModel.deviceListSaved.clear()
                devicesViewModel.deviceListSaved.addAll(state.devices)
                deviceAdapterSaved.notifyDataSetChanged()
            }

            is DeviceSavedState.Error -> {
                binding.errorMessageSaved.text = state.errorMessage
                binding.errorMessageSaved.showAndHideOthers(viewArraySaved)
            }

            is DeviceSavedState.Empty -> {
                binding.emptyListDevicesSaved.showAndHideOthers(viewArraySaved)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderSearchedDevice(state: DeviceSearchedState) {
        when (state) {
            is DeviceSearchedState.Loading -> binding.loadingTimeSearched.showAndHideOthers(viewArraySearched)
            is DeviceSearchedState.FinishSearched -> {
                updateStateBluetoothDevices()
            }
            is DeviceSearchedState.Error -> {
                binding.errorMessageSearched.showAndHideOthers(viewArraySearched)
            }
        }
    }

    private fun updateStateBluetoothDevices() {
        if (devicesViewModel.deviceListSearched.isEmpty()) {
            binding.emptyListDevicesSearched.showAndHideOthers(viewArraySearched)
        } else {
            binding.listBluetoothSearched.showAndHideOthers(viewArraySearched)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderConnectedDevices(macAddress: String) {
        if (macAddress.isNotEmpty()) {
            devicesViewModel.deviceListSaved.forEach { device ->
                if (device.macAddress == macAddress) {
                    device.isReceived = true
                    device.statusConnected = StatusDeviceBluetooth.Connect
                    devicesViewModel.receiveData()
                }
            }
        } else {
            devicesViewModel.deviceListSaved.forEach { device -> device.isReceived = false }
        }
        deviceAdapterSaved.notifyDataSetChanged()
    }

    private val listener: View.OnClickListener = View.OnClickListener { view ->
        when (view?.id) {
            R.id.button_back -> {
                findNavController().navigateUp()
            }

            R.id.search_device -> {
                binding.loadingTimeSearched.showAndHideOthers(viewArraySearched)
                searchBluetoothDeviceClickDebounce(Unit)
            }
        }
    }

    private fun searchBluetoothDevices() {
        permissionBluetooth()
        devicesViewModel.deviceListSearched.clear()
        devicesViewModel.searchBluetoothDevices()
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context, intent: Intent?) {
            if (ActivityCompat.checkSelfPermission(
                    p0.applicationContext,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val deviceBonded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                }

                if (deviceBonded != null && !deviceBonded.name.isNullOrEmpty() && !deviceBonded.address.isNullOrEmpty()) {
                    for (deviceSaved in devicesViewModel.deviceListSaved) {
                        if (deviceBonded.address == deviceSaved.macAddress) {
                            deviceSaved.statusConnected = when (deviceBonded.bondState) {
                                BluetoothDevice.BOND_NONE -> {
                                    StatusDeviceBluetooth.Disconnect
                                }

                                BluetoothDevice.BOND_BONDING -> {
                                    StatusDeviceBluetooth.Connected
                                }

                                BluetoothDevice.BOND_BONDED -> {
                                    StatusDeviceBluetooth.Connect
                                }

                                else -> {
                                    StatusDeviceBluetooth.Invisible
                                }
                            }
                            deviceAdapterSaved.notifyDataSetChanged()
                        }
                    }
                    if (
                        devicesViewModel.deviceListSearched.none { device -> device.macAddress == deviceBonded.address }
                        && devicesViewModel.deviceListSaved.none { device -> device.macAddress == deviceBonded.address }
                    ) {
                        devicesViewModel.deviceListSearched.add(
                            SettingMappers.map(deviceBonded)
                        )
                        deviceAdapterSearched.notifyDataSetChanged()
                        updateStateBluetoothDevices()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.unregisterReceiver(broadcastReceiver)
        devicesViewModel.clearSearchBluetoothDevices()

        _binding = null
        viewArraySaved = null
        viewArraySearched = null
    }
}