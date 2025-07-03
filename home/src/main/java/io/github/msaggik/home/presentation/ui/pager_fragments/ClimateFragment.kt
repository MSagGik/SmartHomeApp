package io.github.msaggik.home.presentation.ui.pager_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.FragmentClimateBinding
import io.github.msaggik.home.domain.model.device.ClimateUIModel
import io.github.msaggik.home.presentation.view_model.pager_view_model.ClimateViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class ClimateFragment : Fragment() {

    private val climateViewModel: ClimateViewModel by viewModel()

    private var _binding: FragmentClimateBinding? = null
    private val binding: FragmentClimateBinding
        get() = _binding
            ?: throw IllegalStateException("Binding should not be called after onDestroy")

    private var nameDevice: String? = null
    private var macAddressDevice: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nameDevice =
            savedInstanceState?.getString(NAME_DEVICE) ?: requireArguments().getString(NAME_DEVICE)
        macAddressDevice =
            savedInstanceState?.getString(MAC_ADDRESS_DEVICE) ?: requireArguments().getString(
                MAC_ADDRESS_DEVICE
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClimateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameDevice?.let { nameBluetoothDevice ->
            binding.nameDevice.text = nameBluetoothDevice
        }

        var lastProcessedMac: String? = null

        climateViewModel.connectedDevices.observe(viewLifecycleOwner) { listMac ->
            macAddressDevice?.let { address ->
                if (listMac.contains(address)) {
                    if (address != lastProcessedMac) {
                        climateViewModel.enablingDataReception(
                            macAddressDevice = address,
                            enabled = true
                        )
                        val hasReceive = climateViewModel.receiveData(address)
                        if (hasReceive) {
                            lastProcessedMac = address
                        }
                    }
                    binding.dataStateClimateLayout.visibility = View.VISIBLE
                    binding.emptyStateClimateLayout.visibility = View.GONE
                } else {
                    lastProcessedMac = null
                    binding.dataStateClimateLayout.visibility = View.GONE
                    binding.emptyStateClimateLayout.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                climateViewModel.climateUiState.collect { data ->
                    renderClimateControl(data)
                }
            }
        }

        binding.connectDevice.setOnClickListener(listener)
    }

    private val listener: View.OnClickListener = View.OnClickListener { view ->
        when (view?.id) {
            R.id.connect_device -> {
                macAddressDevice?.let { address ->
                    climateViewModel.connect(
                        uuid = UUID.fromString(DEFAULT_UUID),
                        macAddress = address
                    )
                    climateViewModel.receiveData(address)
                }
            }
        }
    }

    private fun renderClimateControl(climateUIModel: ClimateUIModel) {
        if (climateUIModel.validData) {
            binding.sensorTemperature.text =
                getString(io.github.msaggik.ui.R.string.sensor_temperature, climateUIModel.temperature)
            binding.sensorHumidity.text =
                getString(io.github.msaggik.ui.R.string.sensor_humidity, climateUIModel.humidity)
            binding.sensorPressure.text =
                getString(io.github.msaggik.ui.R.string.sensor_pressure, climateUIModel.pressure)
        } else {
            binding.sensorTemperature.text = getString(io.github.msaggik.ui.R.string.default_hyphen)
            binding.sensorHumidity.text = getString(io.github.msaggik.ui.R.string.default_hyphen)
            binding.sensorPressure.text = getString(io.github.msaggik.ui.R.string.default_hyphen)
        }
        binding.imageTemperature.setImageResource(climateUIModel.tempIconRes)
        binding.imageHumidity.setImageResource(climateUIModel.humidityIconRes)
        binding.imagePressure.setImageResource(climateUIModel.pressureIconRes)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(NAME_DEVICE, requireArguments().getString(NAME_DEVICE))
        outState.putString(MAC_ADDRESS_DEVICE, requireArguments().getString(MAC_ADDRESS_DEVICE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ClimateFragment"
        private const val DEFAULT_UUID = "00001101-0000-1000-8000-00805F9B34FB" // номер UUID последовательного порта Bluetooth (Serial Port Profile, SPP)
        private const val NAME_DEVICE = "name_device"
        private const val MAC_ADDRESS_DEVICE = "mac_address_device"
        fun newInstance(nameDevice: String, macAddress: String): ClimateFragment {
            return ClimateFragment().apply {
                arguments = bundleOf(
                    NAME_DEVICE to nameDevice,
                    MAC_ADDRESS_DEVICE to macAddress
                )
            }
        }
    }
}