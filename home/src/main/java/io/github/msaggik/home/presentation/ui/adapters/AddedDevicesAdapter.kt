package io.github.msaggik.home.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.github.msaggik.home.databinding.ItemAddedDeviceAddBinding
import io.github.msaggik.home.databinding.ItemAddedDeviceBinding
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.model.device.TypeDeviceRoom

private const val VIEW_TYPE_ITEM = 0
private const val VIEW_TYPE_FOOTER = 1

class AddedDevicesAdapter(
    private val listAddedDevices: MutableList<SmartDeviceRoom>,
    private val deviceClickListener: DeviceClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var list = listAddedDevices

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = ItemAddedDeviceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddedDevicesViewHolder(binding)
            }

            else -> {
                val binding = ItemAddedDeviceAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddDeviceViewHolder(binding)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == list.size) VIEW_TYPE_FOOTER else VIEW_TYPE_ITEM
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AddedDevicesViewHolder -> {
                holder.bind(list[position])
                holder.itemView.setOnLongClickListener {
                    deviceClickListener.onDeleteDeviceLongClick(list[position])
                    true
                }
            }

            is AddDeviceViewHolder -> {
                holder.bind {
                    deviceClickListener.onAddDeviceClick()
                }
            }
        }
    }

    interface DeviceClickListener {
        fun onDeleteDeviceLongClick(deleteDevice: SmartDeviceRoom)
        fun onAddDeviceClick()
    }

    class AddedDevicesViewHolder(
        private val binding: ItemAddedDeviceBinding
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

    class AddDeviceViewHolder(
        private val binding: ItemAddedDeviceAddBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(onAddClick: () -> Unit) {
            binding.itemAddedDeviceAdd.setOnClickListener { onAddClick.invoke() }
        }
    }
}