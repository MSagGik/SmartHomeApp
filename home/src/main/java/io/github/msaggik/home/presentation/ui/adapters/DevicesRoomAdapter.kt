package io.github.msaggik.home.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.msaggik.home.databinding.ItemDeviceRoomBinding
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom

class DevicesRoomAdapter (
    private val listSmartRoomDevice: MutableList<SmartDeviceRoom>,
    private val deviceClickListener: DeviceClickListener
) : RecyclerView.Adapter<DevicesRoomAdapter.DeviceViewHolder> () {

    private var list = listSmartRoomDevice

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val layoutInspector = LayoutInflater.from(parent.context)
        return DeviceViewHolder(ItemDeviceRoomBinding.inflate(layoutInspector, parent, false))
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
        fun onDeviceClick(device: SmartDeviceRoom)
    }

    class DeviceViewHolder(
        private val binding: ItemDeviceRoomBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(device: SmartDeviceRoom) {
            binding.imageTypeDevice.setImageResource(device.typeDevice.imageDevice)
            binding.nameDevice.text = device.name
            binding.macDevice.text = device.macAddress
        }
    }
}