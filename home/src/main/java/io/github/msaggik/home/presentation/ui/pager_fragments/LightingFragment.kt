package io.github.msaggik.home.presentation.ui.pager_fragments

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.FragmentLightingBinding
import io.github.msaggik.home.domain.model.device.LightingUIModel
import io.github.msaggik.home.presentation.view_model.pager_view_model.LightingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class LightingFragment : Fragment() {

    private var parentListener: ParentFragmentListener? = null

    interface ParentFragmentListener {
        fun currentDataLightingMenu(lightingUIModel: LightingUIModel)
        fun turnOnLightingMenu()
        fun setAlphaLighting(value: Int)
        fun setLighting(lightOn: Boolean)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentListener = parentFragment as? ParentFragmentListener
    }

    private val lightingViewModel: LightingViewModel by viewModel()

    private var _binding: FragmentLightingBinding? = null
    private val binding: FragmentLightingBinding
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
        _binding = FragmentLightingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameDevice?.let { nameBluetoothDevice ->
            binding.nameDevice.text = nameBluetoothDevice
        }

        var lastProcessedMac: String? = null

        lightingViewModel.connectedDevices.observe(viewLifecycleOwner) { listMac ->
            macAddressDevice?.let { address ->
                if (listMac.contains(address)) {
                    if (address != lastProcessedMac) {
                        lightingViewModel.enablingDataReception(
                            macAddressDevice = address,
                            enabled = true
                        )
                        val hasReceive = lightingViewModel.receiveData(address)
                        if (hasReceive) {
                            lastProcessedMac = address
                        }
                    }
                    binding.dataStateLightingLayout.visibility = View.VISIBLE
                    binding.emptyStateClimateLayout.visibility = View.GONE
                } else {
                    lastProcessedMac = null
                    binding.dataStateLightingLayout.visibility = View.GONE
                    binding.emptyStateClimateLayout.visibility = View.VISIBLE
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                lightingViewModel.lightingUiState.collect { data ->
                    renderLightingControl(data)
                }
            }
        }

        binding.connectDevice.setOnClickListener(listener)
        binding.buttonMenuLightingMain.setOnClickListener(listener)
        binding.seekBarLighting.setOnSeekBarChangeListener(seekBarChangeListener)
        binding.switchLighting.setOnCheckedChangeListener(switchLightingListener)
    }

    private val listener: View.OnClickListener = View.OnClickListener { view ->
        when (view?.id) {
            R.id.connect_device -> {
                macAddressDevice?.let { address ->
                    lightingViewModel.connect(
                        uuid = UUID.fromString(DEFAULT_UUID),
                        macAddress = address
                    )
                    lightingViewModel.receiveData(address)
                }
            }
            R.id.button_menu_lighting_main -> {
                parentListener?.turnOnLightingMenu()
            }
        }
    }

    private val seekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            binding.valueLighting.text = getString(
                io.github.msaggik.ui.R.string.value_percent,
                convert8bitToPercentValue(progress)
            )
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            if (seekBar != null) {
                parentListener?.setAlphaLighting(seekBar.progress)
                binding.viewLighting.updateTintAlpha(seekBar.progress)
            }
        }
    }

    private val switchLightingListener = CompoundButton.OnCheckedChangeListener { _, checked ->
        parentListener?.setLighting(checked)

        val currentLightingUIModel = if (checked) {
            LightingUIModel.defaultStartLightingUIModel("")
        } else {
            LightingUIModel.defaultNullLightingUIModel("")
        }

        binding.valueLighting.text = getString(
            io.github.msaggik.ui.R.string.value_percent,
            convert8bitToPercentValue(currentLightingUIModel.alpha)
        )
        binding.seekBarLighting.progress = currentLightingUIModel.alpha
        binding.seekBarLighting.isEnabled = checked
        binding.viewLighting.imageTintList = convertLightingUIModeToColorView(currentLightingUIModel)
    }

    private fun renderLightingControl(lightingUIModel: LightingUIModel) {
        val currentLightingUIModel = if (lightingUIModel.validData) {
            lightingUIModel
        } else {
            LightingUIModel.defaultNullLightingUIModel("")
        }

        with(currentLightingUIModel) {
            binding.switchLighting.setChecked(validData && (alpha != 0 || colors[0].red != 0 || colors[0].green != 0 || colors[0].blue != 0))
            if (!validData || binding.seekBarLighting.progress !in (alpha - PERMISSIBLE_ERROR)..(alpha + PERMISSIBLE_ERROR)) {
                binding.seekBarLighting.progress = alpha
            }
            binding.valueLighting.text = getString(
                io.github.msaggik.ui.R.string.value_percent,
                convert8bitToPercentValue(alpha)
            )
            binding.viewLighting.imageTintList = convertLightingUIModeToColorView(this@with)
            parentListener?.currentDataLightingMenu(this@with)
        }

    }

    private fun convert8bitToPercentValue(value: Int): Float = (100.0f * value) / 255.0f
    private fun convertLightingUIModeToColorView(lightingUIModel: LightingUIModel): ColorStateList {
        return with(lightingUIModel) {
            ColorStateList.valueOf(
                Color.argb(
                    alpha,
                    colors[0].red,
                    colors[0].green,
                    colors[0].blue
                )
            )
        }
    }

    private fun ImageView.updateTintAlpha(alpha: Int) {
        imageTintList?.let { csl ->
            val color = csl.getColorForState(drawableState, Color.BLACK)
            imageTintList = ColorStateList.valueOf(
                Color.argb(
                    alpha,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            )
        }
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
        private const val TAG = "LightingFragment"
        private const val PERMISSIBLE_ERROR = 10
        private const val DEFAULT_UUID = "00001101-0000-1000-8000-00805F9B34FB" // номер UUID последовательного порта Bluetooth (Serial Port Profile, SPP)
        private const val NAME_DEVICE = "name_device"
        private const val MAC_ADDRESS_DEVICE = "mac_address_device"
        fun newInstance(nameDevice: String, macAddress: String): LightingFragment {
            return LightingFragment().apply {
                arguments = bundleOf(
                    NAME_DEVICE to nameDevice,
                    MAC_ADDRESS_DEVICE to macAddress
                )
            }
        }
    }
}