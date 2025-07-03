package io.github.msaggik.home.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.ItemLightingModeBinding
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.util.convertDpToPx

class LightingModeAdapter (
    private val lightingModePolies: MutableList<LightingModePoly>,
    private val lightingModeClickListener: LightingModeClickListener
) : RecyclerView.Adapter<LightingModeAdapter.LightingModeViewHolder> () {

    private var list = lightingModePolies

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightingModeViewHolder {
        val layoutInspector = LayoutInflater.from(parent.context)
        return LightingModeViewHolder(ItemLightingModeBinding.inflate(layoutInspector, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: LightingModeViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener{
            lightingModeClickListener.onLightingModeClick(list[position])
        }
        holder.itemView.setOnLongClickListener() {
            lightingModeClickListener.deleteLightingModeClick(list[position])
            true
        }
    }

    interface LightingModeClickListener {
        fun onLightingModeClick(lightingModePoly: LightingModePoly)
        fun deleteLightingModeClick(lightingModePoly: LightingModePoly)
    }

    class LightingModeViewHolder(
        private val binding: ItemLightingModeBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(lightingModePoly: LightingModePoly) {
            lightingModePoly.uriImage?.let { image ->
                if (image != "null") {
                    Glide.with(itemView.context)
                        .load(image)
                        .placeholder(R.drawable.placeholder_setting_lighting)
                        .transform(
                            CenterCrop(),
                            RoundedCorners(
                                itemView.context.applicationContext.convertDpToPx(14f)
                            )
                        )
                        .transform()
                        .into(binding.imageLightingMode)
                }
            }
            binding.textLightingMode.text = lightingModePoly.name
        }
    }
}