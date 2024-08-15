package com.msaggik.settings.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msaggik.settings.databinding.ItemBluetoothBinding
import com.msaggik.settings.domain.model.SmartDevice

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
            binding.nameDevice.text = device.name
            binding.macDevice.text = device.macAddress
            binding.checkDevice.isChecked = device.checked
        }
    }
}