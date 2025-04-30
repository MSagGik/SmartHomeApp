package io.github.msaggik.home.presentation.ui

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.FragmentHomeBinding
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.home.presentation.ui.adapters.RoomHomeAdapter
import io.github.msaggik.home.presentation.view_model.HomeViewModel
import io.github.msaggik.home.presentation.view_model.state.StateRoomListHome
import io.github.msaggik.util.DebounceMode
import io.github.msaggik.util.debounce
import io.github.msaggik.util.showAndHideOthers
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val DELAY_CLICK = 500L

class HomeFragment : Fragment() {

    private val homeViewModel: HomeViewModel by viewModel()

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding
            ?: throw IllegalStateException("Binding should not be called after onDestroy")
    private var viewArraySaved: Array<View>? = null

    private var roomHomeList: MutableList<RoomHome> = mutableListOf()

    private lateinit var roomHomeClickDebounce: (RoomHome) -> Unit

    private val roomHomeAdapter: RoomHomeAdapter by lazy {
        RoomHomeAdapter(
            listRoomHome = roomHomeList,
            roomHomeClickListener = object : RoomHomeAdapter.RoomHomeClickListener {
                override fun onRoomHomeClick(roomHome: RoomHome) {
                    roomHomeClickDebounce(roomHome)
                }

                override fun onRoomLongHomeClick(roomHome: RoomHome) {
                    deleteItemRoomHomeDialog(roomHome.idRoomHome)
                }

                override fun onAddNewItemRequest() {
                    findNavController().navigate(
                        R.id.action_homeFragment_to_newRoomFragment,
                        NewRoomFragment.createArgsNewRoomHome()
                    )
                }
            }
        )
    }

    private fun roomHomeClick(roomHome: RoomHome) {
            findNavController().navigate(
                R.id.action_homeFragment_to_roomHomeFragment,
                RoomHomeFragment.createArgs(roomHomeId = roomHome.idRoomHome)
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel.getRoomsHome()
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewArraySaved = arrayOf(
            binding.loadingRooms,
            binding.roomsList,
            binding.placeholderEmptyRooms,
            binding.placeholderErrorGetRooms
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        roomHomeClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<RoomHome>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) { roomHome -> roomHomeClick(roomHome) }

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.roomsList.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.roomsList.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        binding.roomsList.adapter = roomHomeAdapter

        homeViewModel.getStateRoomListHomeLiveData().observe(viewLifecycleOwner) { state ->
            renderRoomsHome(state)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderRoomsHome(state: StateRoomListHome) {
        when (state) {
            is StateRoomListHome.Loading -> {
                binding.loadingRooms.showAndHideOthers(viewArraySaved)
            }

            is StateRoomListHome.Content -> {
                roomHomeList.clear()
                roomHomeList.addAll(state.rooms.listRoom)
                roomHomeAdapter.notifyDataSetChanged()
                binding.roomsList.showAndHideOthers(viewArraySaved)
            }

            is StateRoomListHome.Empty -> {
                roomHomeList.clear()
                roomHomeAdapter.notifyDataSetChanged()
                binding.roomsList.showAndHideOthers(viewArraySaved)
            }

            is StateRoomListHome.Error -> {
                binding.placeholderErrorGetRooms.showAndHideOthers(viewArraySaved)
            }
        }
    }

    private fun deleteItemRoomHomeDialog(idRoomHome: Long) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(io.github.msaggik.ui.R.string.confirm_delete))
            .setMessage(getString(io.github.msaggik.ui.R.string.confirm_delete_room_home))
            .setNeutralButton(getString(io.github.msaggik.ui.R.string.cancel)) { dialog, which -> }
            .setPositiveButton(getString(io.github.msaggik.ui.R.string.delete_default)) { dialog, which ->
                homeViewModel.deleteItemRoomHome(idRoomHome)
                homeViewModel.getRoomsHome()
            }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewArraySaved = null
    }
}