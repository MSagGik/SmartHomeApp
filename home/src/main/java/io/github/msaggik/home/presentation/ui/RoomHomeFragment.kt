package io.github.msaggik.home.presentation.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.FragmentRoomHomeBinding
import io.github.msaggik.home.domain.model.device.ColorModel
import io.github.msaggik.home.domain.model.device.LightingModeMono
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.home.domain.model.device.LightingModeState
import io.github.msaggik.home.domain.model.device.LightingUIModel
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.model.device.TypeDeviceRoom
import io.github.msaggik.home.presentation.ui.adapters.DevicesAdapter
import io.github.msaggik.home.presentation.ui.adapters.DynamicRoomHomePagerAdapter
import io.github.msaggik.home.presentation.ui.adapters.LightingModeAdapter
import io.github.msaggik.home.presentation.ui.adapters.entity.IotPage
import io.github.msaggik.home.presentation.ui.pager_fragments.AutoFragment
import io.github.msaggik.home.presentation.ui.pager_fragments.ClimateFragment
import io.github.msaggik.home.presentation.ui.pager_fragments.LightingFragment
import io.github.msaggik.home.presentation.ui.pager_fragments.TvFragment
import io.github.msaggik.home.presentation.ui.pager_fragments.UniversalFragment
import io.github.msaggik.home.presentation.view_model.RoomHomeViewModel
import io.github.msaggik.home.presentation.view_model.state.LightingModesState
import io.github.msaggik.home.presentation.view_model.state.PlayPauseLightingModeState
import io.github.msaggik.home.presentation.view_model.state.RoomHomeState
import io.github.msaggik.util.DebounceMode
import io.github.msaggik.util.convertDpToPx
import io.github.msaggik.util.debounce
import io.github.msaggik.util.showAndHideOthers
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class RoomHomeFragment : Fragment(), LightingFragment.ParentFragmentListener {

    private val roomHomeViewModel: RoomHomeViewModel by viewModel()

    override fun currentDataLightingMenu(lightingUIModel: LightingUIModel) {
        roomHomeViewModel.currentLightingUIModel = LightingUIModel(
            validData = lightingUIModel.macAddress.isNotEmpty(),
            macAddress = lightingUIModel.macAddress,
            mode = lightingUIModel.mode,
            alpha = lightingUIModel.alpha,
            colors = lightingUIModel.colors
        )
        roomHomeViewModel.setPlayPauseLightingModeByMode(lightingUIModel.mode)
    }

    override fun turnOnLightingMenu() {
        bottomSheetSettingLighting?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun setAlphaLighting(value: Int) {
        if (roomHomeViewModel.currentLightingUIModel.alpha != value) {
            roomHomeViewModel.currentLightingUIModel =
                roomHomeViewModel.currentLightingUIModel.apply {
                    alpha = value
                }
            roomHomeViewModel.sendStartAlphaColorLightingDataToIotDevice(roomHomeViewModel.currentLightingUIModel)
        }
    }

    override fun setLighting(lightOn: Boolean) {
        roomHomeViewModel.currentLightingUIModel = if (lightOn) {
            LightingUIModel.defaultStartLightingUIModel(roomHomeViewModel.currentLightingUIModel.macAddress)
        } else {
            LightingUIModel.defaultNullLightingUIModel(roomHomeViewModel.currentLightingUIModel.macAddress)
        }
        roomHomeViewModel.sendStartBaseColorLightingDataToIotDevice(roomHomeViewModel.currentLightingUIModel)
    }

    private var _binding: FragmentRoomHomeBinding? = null
    private val binding: FragmentRoomHomeBinding
        get() = _binding
            ?: throw IllegalStateException("Binding should not be called after onDestroy")
    private var viewArrayLightingModes: Array<View>? = null

    private lateinit var roomTabMediator: TabLayoutMediator

    private var bottomSheetBehaviorMenu: BottomSheetBehavior<ConstraintLayout>? = null
    private var bottomSheetSettingLighting: BottomSheetBehavior<ConstraintLayout>? = null
    private var bottomSheetSettingLightingMono: BottomSheetBehavior<ConstraintLayout>? = null

    private val listDeviceRoom: MutableList<SmartDeviceRoom> = mutableListOf()

    private val devicesAdapter: DevicesAdapter by lazy {
        DevicesAdapter(
            listAddedDevices = listDeviceRoom
        )
    }

    private val lightingModePolies: MutableList<LightingModePoly> = mutableListOf()

    private val lightingModeAdapter: LightingModeAdapter by lazy {
        LightingModeAdapter(
            lightingModePolies = lightingModePolies,
            lightingModeClickListener = object : LightingModeAdapter.LightingModeClickListener {
                override fun onLightingModeClick(lightingModePoly: LightingModePoly) {
                    lightingModeClickDebounce(lightingModePoly)
                }

                override fun deleteLightingModeClick(lightingModePoly: LightingModePoly) {
                    deleteLightingMode(lightingModePoly.id)
                }
            }
        )
    }

    private var pagerAdapter: DynamicRoomHomePagerAdapter? = null

    private var idRoomHome = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idRoomHome =
            savedInstanceState?.getLong(ROOM_HOME_ID) ?: requireArguments().getLong(ROOM_HOME_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRoomHomeBinding.inflate(inflater, container, false)
        viewArrayLightingModes = arrayOf(
            binding.loadingLightingList,
            binding.lightingListMode,
            binding.emptyLightingList,
            binding.errorMessageLightingList
        )
        return binding.root
    }

    private lateinit var editRoomHomeClickDebounce: (Unit) -> Unit
    private lateinit var deleteRoomHomeClickDebounce: (Unit) -> Unit
    private lateinit var newLightingModeClickDebounce: (Unit) -> Unit
    private lateinit var lightingModeClickDebounce: (LightingModePoly) -> Unit

    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editRoomHomeClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<Unit>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) { editRoomHomeData() }

        deleteRoomHomeClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<Unit>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) { deleteItemRoomHomeDialog(idRoomHome) }

        newLightingModeClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<Unit>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) { newLightingMode() }

        lightingModeClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<LightingModePoly>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) { lightingMode -> lightingModeClick(lightingMode) }

        defaultBottomSheetState()

        binding.listDevicesRoomHomeMenu.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.listDevicesRoomHomeMenu.adapter = devicesAdapter

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.lightingListMode.layoutManager = GridLayoutManager(
                activity,
                NUMBER_COLUMN_RECYCLERVIEW_LANDSCAPE,
                LinearLayoutManager.VERTICAL,
                false
            )
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.lightingListMode.layoutManager = GridLayoutManager(
                activity,
                NUMBER_COLUMN_RECYCLERVIEW_PORTRAIT,
                LinearLayoutManager.VERTICAL,
                false
            )
        }
        binding.lightingListMode.adapter = lightingModeAdapter
        lightingModeAdapter.notifyDataSetChanged()

        roomHomeViewModel.getItemRoomHome(idRoomHome)
        roomHomeViewModel.getRoomHomeLiveData().observe(viewLifecycleOwner) {
            renderRoomHome(it)
        }

        roomHomeViewModel.getLightingModes()
        roomHomeViewModel.getLightingModesLiveData().observe(viewLifecycleOwner) {
            renderLightingModes(it)
        }

        roomHomeViewModel.getLightingModeLiveData().observe(viewLifecycleOwner) {
            renderLightingMode(it)
        }

        roomHomeViewModel.getPlayPauseLightingModeLiveData().observe(viewLifecycleOwner) {
            renderPlayPauseLightingMode(it)
        }

        pagerAdapter = DynamicRoomHomePagerAdapter(
            fragmentManager = childFragmentManager,
            lifecycle = lifecycle
        )
        binding.roomViewPager.adapter = pagerAdapter
        pagerAdapter?.submitList(listOf())

        roomTabMediator =
            TabLayoutMediator(binding.roomTabLayout, binding.roomViewPager) { tab, position ->
                pagerAdapter?.getItem(position)?.let {
                    tab.text = getString(it.titleResId)
                }
            }

        roomTabMediator.attach()

        binding.buttonMenu.setOnClickListener(listener)
        binding.buttonLightingNewMode.setOnClickListener(listener)
        binding.buttonPlayPause.setOnClickListener(listener)
        binding.imageCurrentModeLightingMono.setOnClickListener(listener)
        binding.buttonLightingMono.setOnClickListener(listener)
        binding.editRoomHomeMenu.setOnClickListener(listener)
        binding.deleteRoomHomeMenu.setOnClickListener(listener)
        binding.buttonBack.setOnClickListener(listener)

        binding.paletteLightingMono.setOnTouchListener(touchListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderRoomHome(state: RoomHomeState) {
        when (state) {
            is RoomHomeState.Loading -> {}
            is RoomHomeState.Content -> {
                binding.headerRoomHome.text = state.roomHome.roomHomeName
                binding.nameRoomHomeMenu.text = state.roomHome.roomHomeName
                if (state.roomHome.roomHomeDescription.isNotEmpty()) {
                    binding.descriptionRoomHomeMenu.text = state.roomHome.roomHomeDescription
                }

                val devicesRoom = state.roomHome.listDevice
                if (devicesRoom.isNotEmpty()) {
                    updatePagerList(devicesRoom)
                    listDeviceRoom.clear()
                    listDeviceRoom.addAll(devicesRoom)
                    devicesAdapter.notifyDataSetChanged()
                    binding.listDevicesRoomHomeMenu.visibility = View.VISIBLE
                } else {
                    binding.listDevicesRoomHomeMenu.visibility = View.GONE
                }

                state.roomHome.roomHomeImage?.let { image ->
                    if (image != "null") {
                        Glide.with(requireContext())
                            .load(image)
                            .placeholder(io.github.msaggik.ui.R.drawable.placeholder_color)
                            .centerCrop()
                            .transform()
                            .into(binding.imageRoomHome)
                        Glide.with(requireContext())
                            .load(image)
                            .placeholder(R.drawable.ic_placeholder_room)
                            .centerCrop()
                            .transform(CenterCrop(), CircleCrop())
                            .into(binding.imageRoomHomeMenu)
                    } else {
                        binding.imageRoomHome.setImageResource(io.github.msaggik.ui.R.drawable.placeholder_color)
                        binding.imageRoomHomeMenu.setImageResource(R.drawable.ic_placeholder_room)
                    }
                } ?: run {
                    binding.imageRoomHome.setImageResource(io.github.msaggik.ui.R.drawable.placeholder_color)
                    binding.imageRoomHomeMenu.setImageResource(R.drawable.ic_placeholder_room)
                }

                // connect devices
                state.roomHome.listDevice.forEach { device ->
                    roomHomeViewModel.connect(
                        uuid = UUID.fromString(DEFAULT_UUID),
                        macAddress = device.macAddress
                    )
                }
            }

            is RoomHomeState.Error -> {}
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderLightingModes(state: LightingModesState) {
        when (state) {
            is LightingModesState.Loading -> {
                binding.loadingLightingList.showAndHideOthers(viewArrayLightingModes)
            }

            is LightingModesState.Content -> {
                lightingModePolies.clear()
                lightingModePolies.addAll(state.lightingModePolies)
                lightingModeAdapter.notifyDataSetChanged()
                if (state.lightingModePolies.isNotEmpty()) {
                    binding.lightingListMode.showAndHideOthers(viewArrayLightingModes)
                } else {
                    binding.emptyLightingList.showAndHideOthers(viewArrayLightingModes)
                }
            }

            is LightingModesState.Error -> {
                binding.errorMessageLightingList.showAndHideOthers(viewArrayLightingModes)
            }
        }
    }

    private fun renderLightingMode(state: LightingModeState) {
        when(state) {
            is LightingModeState.LightingModePolyState -> {
                roomHomeViewModel.currentLightingUIModel.colors = state.lightingModePoly.colors
                roomHomeViewModel.sendListColorLightingDataToIotDevice(roomHomeViewModel.currentLightingUIModel)

                binding.imageCurrentModeLightingPoly.imageTintList = null
                state.lightingModePoly.uriImage?.let { image ->
                    if (image != "null") {
                        Glide.with(requireContext())
                            .load(image)
                            .placeholder(R.drawable.placeholder_setting_lighting)
                            .transform(
                                CenterCrop(),
                                RoundedCorners(
                                    requireContext().convertDpToPx(14f)
                                )
                            )
                            .into(binding.imageCurrentModeLightingPoly)
                    }
                }
                binding.textCurrentModeLightingPoly.text = state.lightingModePoly.name
                roomHomeViewModel.setPlayPauseLightingMode(PlayPauseLightingModeState.Play(state.lightingModePoly.id))
            }
            is LightingModeState.LightingModeMonoState -> {
                roomHomeViewModel.currentLightingUIModel.colors = List(5) { state.lightingModeMono.color }
                roomHomeViewModel.sendStartBaseColorLightingDataToIotDevice(roomHomeViewModel.currentLightingUIModel)

                binding.imageCurrentModeLightingPoly.imageTintList = ColorStateList.valueOf(
                    Color.rgb(
                        state.lightingModeMono.color.red,
                        state.lightingModeMono.color.green,
                        state.lightingModeMono.color.blue
                    )
                )
                binding.textCurrentModeLightingPoly.text = state.lightingModeMono.name
                roomHomeViewModel.setPlayPauseLightingMode(PlayPauseLightingModeState.PlayOff)
            }
        }
    }

    private fun lightingModeClick(lightingModePoly: LightingModePoly) {
        roomHomeViewModel.setLightingModeLiveData(LightingModeState.LightingModePolyState(lightingModePoly))
        roomHomeViewModel.setPlayPauseLightingMode(PlayPauseLightingModeState.Play(idLightingMode = lightingModePoly.id))
    }

    private fun deleteLightingMode(lightingModeId: Long) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(io.github.msaggik.ui.R.string.confirm_delete))
            .setMessage(getString(io.github.msaggik.ui.R.string.confirm_delete_lighting_mode))
            .setNeutralButton(getString(io.github.msaggik.ui.R.string.cancel)) { dialog, which -> }
            .setPositiveButton(getString(io.github.msaggik.ui.R.string.delete_default)) { dialog, which ->
                roomHomeViewModel.deleteLightingModes(lightingModeId)
                roomHomeViewModel.getLightingModes()
            }.show()
    }

    private fun renderPlayPauseLightingMode(state: PlayPauseLightingModeState) {
        binding.buttonPlayPause.setImageResource(state.stateView)
        binding.buttonPlayPause.isClickable = when (state) {
            PlayPauseLightingModeState.PlayOff -> {
                false
            }

            is PlayPauseLightingModeState.Play -> {
                true
            }

            is PlayPauseLightingModeState.Pause -> {
                true
            }
        }
    }

    private fun updatePagerList(devicesRoom: List<SmartDeviceRoom>) {
        val listPager = mutableListOf<IotPage>()
        var count = 0

        devicesRoom.forEach roomLoop@{ room ->
            val usedTypes = mutableSetOf<TypeDeviceRoom>()
            room.listTypeDeviceRoom.forEach typeLoop@{ type ->
                if (usedTypes.contains(type)) return@typeLoop

                when (type) {
                    TypeDeviceRoom.LIGHTING_ROOM -> {
                        listPager.add(
                            IotPage(
                                id = count++,
                                titleResId = io.github.msaggik.ui.R.string.lighting_header_fragment,
                                fragmentCreator = {
                                    LightingFragment.newInstance(
                                        nameDevice = room.name,
                                        macAddress = room.macAddress
                                    )
                                }
                            )
                        )
                    }

                    TypeDeviceRoom.TEMPERATURE_ROOM,
                    TypeDeviceRoom.HUMIDITY_ROOM,
                    TypeDeviceRoom.PRESSURE_ROOM -> {
                        if (!usedTypes.any {
                                it in listOf(
                                    TypeDeviceRoom.TEMPERATURE_ROOM,
                                    TypeDeviceRoom.HUMIDITY_ROOM,
                                    TypeDeviceRoom.PRESSURE_ROOM
                                )
                            }) {
                            listPager.add(
                                IotPage(
                                    id = count++,
                                    titleResId = io.github.msaggik.ui.R.string.climate_header_fragment,
                                    fragmentCreator = {
                                        ClimateFragment.newInstance(
                                            nameDevice = room.name,
                                            macAddress = room.macAddress
                                        )
                                    }
                                )
                            )
                            usedTypes += listOf(
                                TypeDeviceRoom.TEMPERATURE_ROOM,
                                TypeDeviceRoom.HUMIDITY_ROOM,
                                TypeDeviceRoom.PRESSURE_ROOM
                            )
                        }
                        return@typeLoop
                    }

                    TypeDeviceRoom.TV_ROOM -> {
                        listPager.add(
                            IotPage(
                                id = count++,
                                titleResId = io.github.msaggik.ui.R.string.tv_header_fragment,
                                fragmentCreator = { TvFragment.newInstance(room.macAddress) }
                            )
                        )
                    }

                    TypeDeviceRoom.AUTO_ROOM -> {
                        listPager.add(
                            IotPage(
                                id = count++,
                                titleResId = io.github.msaggik.ui.R.string.auto_header_fragment,
                                fragmentCreator = { AutoFragment.newInstance(room.macAddress) }
                            )
                        )
                    }

                    TypeDeviceRoom.KITCHEN_ROOM,
                    TypeDeviceRoom.SMART_ROOM -> {
                        if (!usedTypes.contains(TypeDeviceRoom.KITCHEN_ROOM) &&
                            !usedTypes.contains(TypeDeviceRoom.SMART_ROOM)
                        ) {
                            listPager.add(
                                IotPage(
                                    id = count++,
                                    titleResId = io.github.msaggik.ui.R.string.universal_header_fragment,
                                    fragmentCreator = { UniversalFragment.newInstance(room.macAddress) }
                                )
                            )
                            usedTypes += listOf(
                                TypeDeviceRoom.KITCHEN_ROOM,
                                TypeDeviceRoom.SMART_ROOM
                            )
                        }
                    }
                }

                usedTypes.add(type)
            }
        }

        pagerAdapter?.submitList(listPager)

        if (!roomTabMediator.isAttached) {
            roomTabMediator.attach()
        }
    }

    private fun editRoomHomeData() {
        if (idRoomHome != -1L) {
            findNavController().navigate(
                R.id.action_roomHomeFragment_to_newRoomFragment,
                NewRoomFragment.createArgsEditRoomHome(idRoomHome)
            )
        } else {
            Toast.makeText(
                requireContext(),
                getString(io.github.msaggik.ui.R.string.error_message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun defaultBottomSheetState() {
        bottomSheetBehaviorMenu = BottomSheetBehavior.from(binding.bottomSheetMenu).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetSettingLighting =
            BottomSheetBehavior.from(binding.bottomSheetSettingLighting).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
            }

        bottomSheetSettingLightingMono =
            BottomSheetBehavior.from(binding.bottomSheetSettingLightingMono).apply {
                state = BottomSheetBehavior.STATE_HIDDEN
            }

        bottomSheetBehaviorMenu?.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetSettingLighting?.addBottomSheetCallback(bottomSheetCallback)
        bottomSheetSettingLightingMono?.addBottomSheetCallback(bottomSheetCallback)
    }

    private val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.foregroundScreen.background = ColorDrawable(
                            ContextCompat.getColor(
                                requireContext(),
                                android.R.color.transparent
                            )
                        )
                    }

                    else -> {
                        binding.foregroundScreen.background = ColorDrawable(
                            ContextCompat.getColor(
                                requireContext(),
                                io.github.msaggik.ui.R.color.app_color_background
                            )
                        )
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.baseLayoutRoomHome.alpha = 1 - slideOffset
            }
        }

    private val listener: View.OnClickListener = View.OnClickListener { view ->
        when (view?.id) {
            R.id.button_menu -> {
                bottomSheetBehaviorMenu?.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            R.id.button_lighting_new_mode -> {
                newLightingModeClickDebounce(Unit)
            }

            R.id.button_play_pause -> {
                roomHomeViewModel.setDrivePlayPauseLightingMode()
            }

            R.id.image_current_mode_lighting_mono -> {
                bottomSheetSettingLightingMono?.state = BottomSheetBehavior.STATE_COLLAPSED
            }

            R.id.button_lighting_mono -> {
                roomHomeViewModel.bufferColorModel?.let {
                    roomHomeViewModel.setLightingModeLiveData(
                        LightingModeState.LightingModeMonoState(
                            LightingModeMono(
                                name = ContextCompat.getString(requireContext(), io.github.msaggik.ui.R.string.mono),
                                color = it
                            )
                        )
                    )
                }
                bottomSheetSettingLightingMono?.state = BottomSheetBehavior.STATE_HIDDEN
            }

            R.id.edit_room_home_menu -> {
                editRoomHomeClickDebounce(Unit)
            }

            R.id.delete_room_home_menu -> {
                deleteRoomHomeClickDebounce(Unit)
            }

            R.id.button_back -> {
                findNavController().navigateUp()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val touchListener: OnTouchListener = OnTouchListener { _, event ->
        var colorModel: ColorModel? = null
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val bitmap = drawableToBitmap(binding.paletteLightingMono)
                val x = event.x.toInt()
                val y = event.y.toInt()

                if (x in 0 until bitmap.width && y in 0 until bitmap.height &&
                    !(x <= TOUCH_ACCURACY && y <= TOUCH_ACCURACY) &&
                    !(x <= TOUCH_ACCURACY && y >= bitmap.height - TOUCH_ACCURACY) &&
                    !(x >= bitmap.width - TOUCH_ACCURACY && y <= TOUCH_ACCURACY) &&
                    !(x >= bitmap.width - TOUCH_ACCURACY && y >= bitmap.height - TOUCH_ACCURACY)
                ) {
                    try {
                        val pixel = bitmap.getPixel(x, y)
                        colorModel = ColorModel(
                            red = Color.red(pixel),
                            green = Color.green(pixel),
                            blue = Color.blue(pixel)
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "[touchListener] error message: ${e.message.toString()}")
                    }
                }

                colorModel?.let { color ->
                    roomHomeViewModel.bufferColorModel = color
                    binding.textColorLightingMono.text = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
                    binding.textColorLightingMono.setTextColor(Color.rgb(color.red, color.green, color.blue))
                    binding.buttonLightingMono.isEnabled = true
                    binding.buttonLightingMono.isClickable = true
                } ?: run {
                    binding.buttonLightingMono.isEnabled = false
                    binding.buttonLightingMono.isClickable = false
                }
            }
        }
        true
    }

    private fun drawableToBitmap(imageView: ImageView): Bitmap {
        val drawable = imageView.drawable ?: throw IllegalArgumentException("Drawable is null")
        val bitmap = Bitmap.createBitmap(
            imageView.width,
            imageView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun newLightingMode() {
        findNavController().navigate(
            R.id.action_roomHomeFragment_to_newLightingModeFragment,
            NewLightingModeFragment.createArgsNewLightingMode()
        )
    }

    private fun deleteItemRoomHomeDialog(idRoomHome: Long) {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(io.github.msaggik.ui.R.string.confirm_delete))
            .setMessage(getString(io.github.msaggik.ui.R.string.confirm_delete_room_home))
            .setNeutralButton(getString(io.github.msaggik.ui.R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(io.github.msaggik.ui.R.string.delete_default)) { _, _ ->
                if (idRoomHome != -1L) {
                    roomHomeViewModel.deleteItemRoomHome(idRoomHome)
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(io.github.msaggik.ui.R.string.error_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(ROOM_HOME_ID, requireArguments().getLong(ROOM_HOME_ID))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetBehaviorMenu?.removeBottomSheetCallback(bottomSheetCallback)
        bottomSheetSettingLighting?.removeBottomSheetCallback(bottomSheetCallback)
        bottomSheetSettingLightingMono?.removeBottomSheetCallback(bottomSheetCallback)
        if (::roomTabMediator.isInitialized) roomTabMediator.detach()
        Glide.with(requireContext().applicationContext).clear(binding.imageRoomHome)
        Glide.with(requireContext().applicationContext).clear(binding.imageRoomHomeMenu)
        _binding = null
        viewArrayLightingModes = null
        bottomSheetBehaviorMenu = null
        bottomSheetSettingLighting = null
        pagerAdapter = null
    }

    companion object {
        private const val TAG = "RoomHomeFragment"
        private const val TOUCH_ACCURACY = 70
        private const val NUMBER_COLUMN_RECYCLERVIEW_LANDSCAPE = 7
        private const val NUMBER_COLUMN_RECYCLERVIEW_PORTRAIT = 3
        private const val DEFAULT_UUID =
            "00001101-0000-1000-8000-00805F9B34FB" // номер UUID последовательного порта Bluetooth (Serial Port Profile, SPP)
        private const val DELAY_CLICK = 250L
        private const val ROOM_HOME_ID = "room_home_id"
        fun createArgs(roomHomeId: Long): Bundle {
            return bundleOf(
                ROOM_HOME_ID to roomHomeId
            )
        }
    }
}