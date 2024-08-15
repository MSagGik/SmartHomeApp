package com.msaggik.settings.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msaggik.common_util.Utils
import com.msaggik.settings.R
import com.msaggik.settings.databinding.FragmentDevicesBinding
import com.msaggik.settings.domain.model.SmartDevice
import com.msaggik.settings.presentation.ui.adapters.DevicesAdapter
import com.msaggik.settings.presentation.ui.state.DeviceState
import com.msaggik.settings.presentation.view_model.DevicesViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class DevicesFragment : Fragment() {

    private val devicesViewModel: DevicesViewModel by viewModel()
    private val bluetoothAdapter: BluetoothAdapter by inject()

    private var viewArray: Array<View>? = null

    private var _binding: FragmentDevicesBinding? = null
    private val binding: FragmentDevicesBinding get() = _binding!!

    private var deviceList: MutableList<SmartDevice> = mutableListOf()

    private val deviceAdapter: DevicesAdapter by lazy {
        DevicesAdapter(deviceList) {
            deviceSelection(it)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deviceSelection(device: SmartDevice) {
        deviceList.map { smartDevice ->
            smartDevice.checked = if (smartDevice.macAddress == device.macAddress) true else false
            deviceAdapter.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDevicesBinding.inflate(inflater, container, false)
        viewArray = arrayOf(
            binding.loadingTime,
            binding.listBluetooth,
            binding.errorMessage,
            binding.emptyListDevices
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        intentFilter()

        binding.listBluetooth.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.listBluetooth.adapter = deviceAdapter

        devicesViewModel.getDevices()

        devicesViewModel.getStateLiveData().observe(viewLifecycleOwner) {
            render(it)
        }

        binding.buttonBack.setOnClickListener(listener)
        binding.searchDevice.setOnClickListener(listener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun render(state: DeviceState) {
        when (state) {
            is DeviceState.Loading -> Utils.visibilityView(viewArray, binding.loadingTime)
            is DeviceState.Content -> {
                Utils.visibilityView(viewArray, binding.listBluetooth)
                deviceList.clear()
                deviceList.addAll(state.devices)
                deviceAdapter.notifyDataSetChanged()
            }

            is DeviceState.Error -> {
                binding.errorMessage.text = state.errorMessage
                Utils.visibilityView(viewArray, binding.errorMessage)
            }

            is DeviceState.Empty -> {
                Utils.visibilityView(viewArray, binding.emptyListDevices)
            }
        }
    }

    private val listener: View.OnClickListener = object : View.OnClickListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onClick(p0: View?) {
            when (p0?.id) {
                R.id.button_back -> {
                    findNavController().popBackStack()
                }

                R.id.search_device -> {
                    Utils.visibilityView(viewArray, binding.listBluetooth)
                    deviceList.clear()
                    binding.headerTwo.text = getString(com.msaggik.common_ui.R.string.search_devices)
                    try {
                        bluetoothAdapter.startDiscovery()
                    } catch (e: SecurityException) {
                    }
                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        override fun onReceive(p0: Context, intent: Intent?) {
            if (ActivityCompat.checkSelfPermission(p0.applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                }

                if(device != null && !device.name.isNullOrEmpty() && !device.address.isNullOrEmpty()) {
                    for (deviceCurrent in deviceList) {
                        if(deviceCurrent.macAddress == device.address) {
                            bluetoothAdapter.cancelDiscovery()
                            if (deviceList.isEmpty()) {
                                binding.emptyListDevices.text =
                                    getString(com.msaggik.common_ui.R.string.no_devices_found)
                                Utils.visibilityView(viewArray, binding.emptyListDevices)
                            }
                            return
                        }
                    }
                    deviceList.add(
                        SmartDevice(
                            device.name,
                            device.address,
                            false
                        )
                    )
                    deviceAdapter.notifyDataSetChanged()
                }
            }
        }

    }

    private fun intentFilter() {
        activity?.registerReceiver(broadcastReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewArray = emptyArray()
        viewArray = null
        deviceList.clear()
        try {
            bluetoothAdapter.cancelDiscovery()
        } catch (e: SecurityException) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.unregisterReceiver(broadcastReceiver)
    }
}