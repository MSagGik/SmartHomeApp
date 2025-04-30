package io.github.msaggik.home.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.ItemRoomsListAddBinding
import io.github.msaggik.home.databinding.ItemRoomsListBinding
import io.github.msaggik.home.domain.model.device.TypeDeviceRoom
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.util.convertDpToPx

private const val VIEW_TYPE_ITEM = 0
private const val VIEW_TYPE_FOOTER = 1

class RoomHomeAdapter (
    private val listRoomHome: MutableList<RoomHome>,
    private val roomHomeClickListener: RoomHomeClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder> () {

    private var list = listRoomHome

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val binding = ItemRoomsListBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                RoomHomeViewHolder(binding)
            }
            else -> {
                val binding = ItemRoomsListAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                AddRoomHomeViewHolder(binding)
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
            is RoomHomeViewHolder -> {
                holder.bind(list[position])
                holder.itemView.setOnClickListener {
                    roomHomeClickListener.onRoomHomeClick(list[position])
                }
                holder.itemView.setOnLongClickListener() {
                    roomHomeClickListener.onRoomLongHomeClick(list[position])
                    true
                }
            }
            is AddRoomHomeViewHolder -> {
                holder.bind {
                    roomHomeClickListener.onAddNewItemRequest()
                }
            }
        }
    }

    interface RoomHomeClickListener {
        fun onRoomHomeClick(roomHome: RoomHome)
        fun onRoomLongHomeClick(roomHome: RoomHome)
        fun onAddNewItemRequest()
    }

    class RoomHomeViewHolder(
        private val binding: ItemRoomsListBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(roomHome: RoomHome) {

            binding.nameRoom.text = roomHome.roomHomeName
            roomHome.roomHomeImage?.let {

            }
            val isValidUri = !roomHome.roomHomeImage.isNullOrEmpty() && roomHome.roomHomeImage != "null"
            if (isValidUri) {
                Glide.with(itemView.context)
                    .load(roomHome.roomHomeImage)
                    .placeholder(R.drawable.placeholder_room)
                    .transform(
                        CenterCrop(),
                        RoundedCorners(
                            itemView.context.applicationContext.convertDpToPx(16f)
                        )
                    )
                    .into(binding.imageRoom)
            } else {
                binding.imageRoom.setImageResource(R.drawable.placeholder_room)
            }

            binding.imageTypeDeviceLighting.visibility = View.GONE
            binding.imageTypeDeviceKitchen.visibility = View.GONE
            binding.imageTypeDeviceSmart.visibility = View.GONE
            binding.imageTypeDeviceTv.visibility = View.GONE
            binding.imageTypeDeviceAuto.visibility = View.GONE
            binding.imageTypeDeviceTemp.visibility = View.GONE
            binding.imageTypeDeviceHumidity.visibility = View.GONE
            binding.imageTypeDevicePressure.visibility = View.GONE

            roomHome.listDevice.forEach { deviceRoom ->
                deviceRoom.listTypeDeviceRoom.forEach { typeDeviceRoom ->
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

    class AddRoomHomeViewHolder(
        private val binding: ItemRoomsListAddBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(onAddClick: () -> Unit) {
            binding.itemAddRoom.setOnClickListener { onAddClick.invoke() }
        }
    }
}