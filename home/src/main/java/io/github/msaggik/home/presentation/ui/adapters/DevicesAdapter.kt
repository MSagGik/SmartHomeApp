package io.github.msaggik.home.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.github.msaggik.home.databinding.ItemDeviceRoomHomeBinding
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.model.device.TypeDeviceRoom

class DevicesAdapter(
    private val listAddedDevices: MutableList<SmartDeviceRoom>
) : RecyclerView.Adapter<DevicesAdapter.AddedDevicesViewHolder>() {

    private var list = listAddedDevices

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddedDevicesViewHolder {
        val binding = ItemDeviceRoomHomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AddedDevicesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: AddedDevicesViewHolder, position: Int) {
        holder.bind(list[position])
    }

    class AddedDevicesViewHolder(
        private val binding: ItemDeviceRoomHomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(device: SmartDeviceRoom) {

            binding.imageDevice.setImageResource(device.typeDevice.imageDevice)
            binding.nameDevice.text = device.name

            binding.imageTypeDeviceLighting.visibility = View.GONE
            binding.imageTypeDeviceKitchen.visibility = View.GONE
            binding.imageTypeDeviceSmart.visibility = View.GONE
            binding.imageTypeDeviceTv.visibility = View.GONE
            binding.imageTypeDeviceAuto.visibility = View.GONE
            binding.imageTypeDeviceTemp.visibility = View.GONE
            binding.imageTypeDeviceHumidity.visibility = View.GONE
            binding.imageTypeDevicePressure.visibility = View.GONE

            device.listTypeDeviceRoom.forEach { typeDeviceRoom ->
                when (typeDeviceRoom) {
                    TypeDeviceRoom.LIGHTING_ROOM -> {
                        binding.imageTypeDeviceLighting.isVisible = true
                    }

                    TypeDeviceRoom.KITCHEN_ROOM -> {
                        binding.imageTypeDeviceKitchen.isVisible = true
                    }

                    TypeDeviceRoom.SMART_ROOM -> {
                        binding.imageTypeDeviceSmart.isVisible = true
                    }

                    TypeDeviceRoom.TV_ROOM -> {
                        binding.imageTypeDeviceTv.isVisible = true
                    }

                    TypeDeviceRoom.AUTO_ROOM -> {
                        binding.imageTypeDeviceAuto.isVisible = true
                    }

                    TypeDeviceRoom.TEMPERATURE_ROOM -> {
                        binding.imageTypeDeviceTemp.isVisible = true
                    }

                    TypeDeviceRoom.HUMIDITY_ROOM -> {
                        binding.imageTypeDeviceHumidity.isVisible = true
                    }

                    TypeDeviceRoom.PRESSURE_ROOM -> {
                        binding.imageTypeDevicePressure.isVisible = true
                    }
                }
            }
        }
    }
}