package io.github.msaggik.home.presentation.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import io.github.msaggik.home.presentation.ui.adapters.entity.IotPage

class DynamicRoomHomePagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val differ = AsyncListDiffer(this, object : DiffUtil.ItemCallback<IotPage>() {
        override fun areItemsTheSame(oldItem: IotPage, newItem: IotPage): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: IotPage, newItem: IotPage): Boolean =
            oldItem == newItem
    })

    fun submitList(pages: List<IotPage>) {
        differ.submitList(pages)
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun createFragment(position: Int): Fragment {
        return differ.currentList[position].fragmentCreator()
    }

    fun getItem(position: Int): IotPage = differ.currentList[position]
}