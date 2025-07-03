package io.github.msaggik.settings.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.github.msaggik.settings.databinding.ItemBluetoothBinding
import io.github.msaggik.settings.domain.model.SmartDevice
import io.github.msaggik.settings.domain.model.state.StatusDeviceBluetooth

class DevicesAdapter (
    private val listSmartDevice: MutableList<SmartDevice>,
    private val deviceClickListener: DeviceClickListener
) : RecyclerView.Adapter<DevicesAdapter.DeviceViewHolder> () {

    private var list = listSmartDevice

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val layoutInspector = LayoutInflater.from(parent.context)
        return DeviceViewHolder(ItemBluetoothBinding.inflate(layoutInspector, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener{
            deviceClickListener.onDeviceClick(list[position])
        }
    }

    fun interface DeviceClickListener {
        fun onDeviceClick(device: SmartDevice)
    }

    class DeviceViewHolder(
        private val binding: ItemBluetoothBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(device: SmartDevice) {
            binding.imageTypeDevice.setImageResource(device.typeDeviceBluetooth.imageDevice)
            binding.nameDevice.text = device.name
            binding.macDevice.text = device.macAddress
            device.statusConnected.let { status ->
                if (status == StatusDeviceBluetooth.Invisible) {
                    binding.imageStatusDevice.visibility = View.INVISIBLE
                } else {
                    binding.imageStatusDevice.setImageResource(device.statusConnected.imageStatusDevice)
                    binding.imageStatusDevice.visibility = View.VISIBLE
                }
            }
            binding.imageConnectDevice.isVisible = device.isReceived
        }
    }
}