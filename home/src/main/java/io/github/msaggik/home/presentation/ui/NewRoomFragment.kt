package io.github.msaggik.home.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.DialogAddDeviceBinding
import io.github.msaggik.home.databinding.DialogCreateImageBinding
import io.github.msaggik.home.databinding.FragmentNewRoomBinding
import io.github.msaggik.home.domain.model.device.SmartDeviceRoom
import io.github.msaggik.home.domain.model.device.TypeDeviceRoom
import io.github.msaggik.home.domain.model.room.RoomHome
import io.github.msaggik.home.presentation.ui.adapters.AddedDevicesAdapter
import io.github.msaggik.home.presentation.ui.adapters.DevicesRoomAdapter
import io.github.msaggik.util.deleteImageFile
import io.github.msaggik.util.onIsNullOrEmptyChange
import io.github.msaggik.util.refreshImageUri
import io.github.msaggik.util.uriImageForApp
import io.github.msaggik.home.presentation.view_model.NewRoomViewModel
import io.github.msaggik.home.presentation.view_model.state.DeviceRoomSavedState
import io.github.msaggik.home.presentation.view_model.state.RoomHomeState
import io.github.msaggik.util.DebounceMode
import io.github.msaggik.util.convertDpToPx
import io.github.msaggik.util.debounce
import io.github.msaggik.util.showAndHideOthers
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewRoomFragment : Fragment() {

    private val newRoomViewModel: NewRoomViewModel by viewModel()

    private var _binding: FragmentNewRoomBinding? = null
    private val binding: FragmentNewRoomBinding get() = _binding ?: throw IllegalStateException("Binding should not be called after onDestroy")
    private var viewArraySaved: Array<View>? = null

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryMedia: ActivityResultLauncher<PickVisualMediaRequest>

    private var bottomSheetBehaviorSavedDevices: BottomSheetBehavior<ConstraintLayout>? = null

    private var isEditRoomHome: Boolean = false
    private var editRoomHomeId: Long = -1L
    private var bufferRoomHome: RoomHome? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            isEditRoomHome = savedInstanceState.getBoolean(IS_EDIT_ROOM_HOME_DATA)
            editRoomHomeId = savedInstanceState.getLong(EDIT_ROOM_HOME_ID)
        } else {
            isEditRoomHome = requireArguments().getBoolean(IS_EDIT_ROOM_HOME_DATA)
            editRoomHomeId = requireArguments().getLong(EDIT_ROOM_HOME_ID)
        }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    drawImage(newRoomViewModel.imageURI)
                }
            }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    openCamera()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(io.github.msaggik.ui.R.string.permission_to_use_camera_not_granted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        galleryMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                newRoomViewModel.imageURI?.let { iUri -> requireContext().deleteImageFile(iUri) }
                newRoomViewModel.imageURI = requireContext().uriImageForApp(uri)
                drawImage(newRoomViewModel.imageURI)
            }
        }
    }

    private fun drawImage(uriImage: Uri?) {
        uriImage?.let {
            Glide.with(requireContext())
                .load(it)
                .placeholder(R.drawable.placeholder_room_add)
                .transform(
                    CenterCrop(),
                    RoundedCorners(
                        requireContext().applicationContext.convertDpToPx(10f)
                    )
                )
                .into(binding.imageRoom)
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            newRoomViewModel.imageURI = requireContext().refreshImageUri(newRoomViewModel.imageURI)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, newRoomViewModel.imageURI)
            cameraLauncher.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(io.github.msaggik.ui.R.string.permission_to_use_camera_not_granted),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val listDeviceRoom: MutableList<SmartDeviceRoom> = mutableListOf()

    private val addedDevicesAdapter: AddedDevicesAdapter by lazy {
        AddedDevicesAdapter(
            listAddedDevices = listDeviceRoom,
            deviceClickListener = object : AddedDevicesAdapter.DeviceClickListener {

                override fun onDeleteDeviceLongClick(deleteDevice: SmartDeviceRoom) {
                    newRoomViewModel.deleteBluetoothDeviceFromListLiveData(deleteDevice)
                }

                override fun onAddDeviceClick() {
                    bottomSheetBehaviorSavedDevices?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
        )
    }

    private val devicesRoomAdapter: DevicesRoomAdapter by lazy {
        DevicesRoomAdapter(newRoomViewModel.deviceListSaved) {
            deviceSelectionSaved(it)
        }
    }

    private fun deviceSelectionSaved(selectedDevice: SmartDeviceRoom) {
        addDeviceDialog(selectedDevice)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewRoomBinding.inflate(inflater, container, false)
        viewArraySaved = arrayOf(
            binding.loadingDevice,
            binding.listBluetoothSaved,
            binding.errorMessageSaved,
            binding.emptyListDevicesSaved
        )
        if (isEditRoomHome) {
            binding.header.text = getString(io.github.msaggik.ui.R.string.edit_room)
            binding.buttonCreate.text = getString(io.github.msaggik.ui.R.string.edit)
        }
        return binding.root
    }

    private var nameRoomEditTextWatcher: TextWatcher? = null

    private lateinit var setRoomHomeWithDevicesClickDebounce: (Unit) -> Unit

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRoomHomeWithDevicesClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<Unit>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) {  createRoomHomeData() }

        defaultBottomSheetState()

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.listAddedDevices.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            binding.listBluetoothSaved.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.listAddedDevices.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.listBluetoothSaved.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        binding.listAddedDevices.adapter = addedDevicesAdapter
        binding.listBluetoothSaved.adapter = devicesRoomAdapter

        drawImage(newRoomViewModel.imageURI)

        newRoomViewModel.getSavedDevices()

        newRoomViewModel.getStateSavedBluetoothDeviceLiveData().observe(viewLifecycleOwner) {
            renderSavedDevice(it)
        }

        newRoomViewModel.getStateAddedBluetoothDevicesLiveData().observe(viewLifecycleOwner) {
            renderAddedBluetoothDevices(it)
        }

        if (isEditRoomHome) {
            newRoomViewModel.getItemRoomHome(idRoomHome = editRoomHomeId)
            newRoomViewModel.getRoomHomeLiveData().observe(viewLifecycleOwner) {
                renderEditRoomHomeViewModel(it)
            }
        }

        binding.imageRoom.setOnClickListener(listener)
        binding.buttonCreate.setOnClickListener(listener)
        binding.buttonBack.setOnClickListener(listener)

        nameRoomEditTextWatcher = binding.nameRoomInput.onIsNullOrEmptyChange { isNullOrEmpty ->
            binding.buttonCreate.isEnabled = !isNullOrEmpty
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderEditRoomHomeViewModel(state: RoomHomeState) {
        when (state) {
            is RoomHomeState.Loading -> {}
            is RoomHomeState.Content -> {
                bufferRoomHome = state.roomHome // for equals exit fragment

                binding.nameRoomInput.setText(state.roomHome.roomHomeName)
                if(state.roomHome.roomHomeDescription.isNotEmpty()) {
                    binding.descriptionRoomInput.setText(state.roomHome.roomHomeDescription)
                }

                newRoomViewModel.addAllBluetoothDeviceInListLiveData(state.roomHome.listDevice.toMutableList())

                state.roomHome.roomHomeImage?.let {
                    newRoomViewModel.imageURI = it.toUri()
                    drawImage(it.toUri())
                }
            }
            is RoomHomeState.Error -> {}
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderAddedBluetoothDevices(deviceList: MutableList<SmartDeviceRoom>) {
        listDeviceRoom.clear()
        listDeviceRoom.addAll(deviceList)
        addedDevicesAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun renderSavedDevice(state: DeviceRoomSavedState) {
        when (state) {
            is DeviceRoomSavedState.Loading -> binding.loadingDevice.showAndHideOthers(viewArraySaved)
            is DeviceRoomSavedState.Content -> {
                binding.listBluetoothSaved.showAndHideOthers(viewArraySaved)
                newRoomViewModel.deviceListSaved.clear()
                newRoomViewModel.deviceListSaved.addAll(state.devices)
                devicesRoomAdapter.notifyDataSetChanged()
            }

            is DeviceRoomSavedState.Error -> {
                binding.errorMessageSaved.text = state.errorMessage
                binding.errorMessageSaved.showAndHideOthers(viewArraySaved)
            }

            is DeviceRoomSavedState.Empty -> {
                binding.emptyListDevicesSaved.showAndHideOthers(viewArraySaved)
            }
        }
    }

    private fun defaultBottomSheetState() {
        bottomSheetBehaviorSavedDevices = BottomSheetBehavior.from(binding.bottomSheetRoom).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        bottomSheetBehaviorSavedDevices?.addBottomSheetCallback(bottomSheetCallback)
    }

    private val bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback =
        object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (bottomSheet.id == R.id.bottom_sheet_room) {
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
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.newRoom.alpha = 1 - slideOffset
            }
        }

    private var listener: View.OnClickListener? = View.OnClickListener { p0 ->
        when (p0?.id) {
            R.id.image_room -> {
                createImageDialog()
            }
            R.id.button_create -> {
                setRoomHomeWithDevicesClickDebounce(Unit)
            }
            R.id.button_back -> {
                backFragment()
            }
        }
    }

    private fun createRoomHomeData() {
        val newRoom = if(isEditRoomHome && editRoomHomeId != -1L) {
            RoomHome(
                idRoomHome = editRoomHomeId,
                roomHomeName = binding.nameRoomInput.text.toString(),
                roomHomeDescription = binding.descriptionRoomInput.text.toString(),
                roomHomeImage = newRoomViewModel.imageURI.toString(),
                listDevice = listDeviceRoom,
                dateModify = System.currentTimeMillis()
            )
        } else {
            RoomHome(
                roomHomeName = binding.nameRoomInput.text.toString(),
                roomHomeDescription = binding.descriptionRoomInput.text.toString(),
                roomHomeImage = newRoomViewModel.imageURI.toString(),
                listDevice = listDeviceRoom,
                dateModify = System.currentTimeMillis()
            )
        }
        newRoomViewModel.setRoomHomeWithDevices(newRoom)
        findNavController().navigateUp()
    }

    private fun createImageDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val dialogViewBinding = DialogCreateImageBinding.inflate(layoutInflater)
        builder.setView(dialogViewBinding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(io.github.msaggik.ui.R.drawable.shape_card)

        dialogViewBinding.photo.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            } else {
                openCamera()
            }
            dialog.dismiss()
        }
        dialogViewBinding.gallery.setOnClickListener {
            galleryMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            dialog.dismiss()
        }

        dialog.setCancelable(true)

        dialog.show()

        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.9f).toInt()
        dialog.window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun addDeviceDialog(deviceRoom: SmartDeviceRoom) {
        val builder = AlertDialog.Builder(requireContext())
        val dialogViewBinding = DialogAddDeviceBinding.inflate(layoutInflater)
        builder.setView(dialogViewBinding.root)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(io.github.msaggik.ui.R.drawable.shape_card)

        dialogViewBinding.imageDeviceDialog.setImageResource(deviceRoom.typeDevice.imageDevice)
        dialogViewBinding.nameDeviceDialog.text = deviceRoom.name
        dialogViewBinding.macDeviceDialog.text = deviceRoom.macAddress
        deviceRoom.listTypeDeviceRoom.clear()

        dialogViewBinding.switchLightingDialog.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.LIGHTING_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.LIGHTING_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.LIGHTING_ROOM)
            }
        }
        dialogViewBinding.switchTemperatureDialog.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.TEMPERATURE_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.TEMPERATURE_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.TEMPERATURE_ROOM)
            }
        }
        dialogViewBinding.switchHumidityDialog.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.HUMIDITY_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.HUMIDITY_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.HUMIDITY_ROOM)
            }
        }
        dialogViewBinding.switchPressureDialog.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.PRESSURE_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.PRESSURE_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.PRESSURE_ROOM)
            }
        }
        dialogViewBinding.switchKitchenDialog.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.KITCHEN_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.KITCHEN_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.KITCHEN_ROOM)
            }
        }
        dialogViewBinding.switchTvDialog.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.TV_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.TV_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.TV_ROOM)
            }
        }
        dialogViewBinding.switchAutoDialog.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.AUTO_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.AUTO_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.AUTO_ROOM)
            }
        }
        dialogViewBinding.switchUniversalSmartDialo.setOnCheckedChangeListener { _, checked ->
            if (checked && !deviceRoom.listTypeDeviceRoom.contains(TypeDeviceRoom.SMART_ROOM)) {
                deviceRoom.listTypeDeviceRoom.add(TypeDeviceRoom.SMART_ROOM)
            } else {
                deviceRoom.listTypeDeviceRoom.remove(TypeDeviceRoom.SMART_ROOM)
            }
        }

        dialogViewBinding.buttonCancel.setOnClickListener {
            dialogViewBinding.switchLightingDialog.setOnCheckedChangeListener(null)
            dialogViewBinding.switchTemperatureDialog.setOnCheckedChangeListener(null)
            dialogViewBinding.switchHumidityDialog.setOnCheckedChangeListener(null)
            dialogViewBinding.switchPressureDialog.setOnCheckedChangeListener(null)
            dialogViewBinding.switchKitchenDialog.setOnCheckedChangeListener(null)
            dialogViewBinding.switchTvDialog.setOnCheckedChangeListener(null)
            dialogViewBinding.switchAutoDialog.setOnCheckedChangeListener(null)
            dialogViewBinding.switchUniversalSmartDialo.setOnCheckedChangeListener(null)
            dialog.dismiss()
        }

        dialogViewBinding.buttonAdd.setOnClickListener {
            newRoomViewModel.addBluetoothDeviceInListLiveData(deviceRoom)
            bottomSheetBehaviorSavedDevices?.state = BottomSheetBehavior.STATE_HIDDEN
            dialog.dismiss()
        }

        dialog.setCancelable(true)

        dialog.show()

        val displayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.9f).toInt()
        dialog.window?.setLayout(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
    }

    private fun backFragmentDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(io.github.msaggik.ui.R.string.confirm_exit))
            .setMessage(getString(io.github.msaggik.ui.R.string.confirm_exit_description_room))
            .setNeutralButton(getString(io.github.msaggik.ui.R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(io.github.msaggik.ui.R.string.exit)) { _, _ ->
                newRoomViewModel.imageURI?.let { requireContext().deleteImageFile(it) }
                findNavController().navigateUp()
            }.show()
    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                backFragment()
            }
        }

    private fun backFragment() {
        if (!isEditRoomHome && (newRoomViewModel.imageURI != null
            || !binding.nameRoomInput.text.isNullOrEmpty()
            || !binding.descriptionRoomInput.text.isNullOrEmpty()
            || listDeviceRoom.isNotEmpty())) {
            backFragmentDialog()
        } else if (isEditRoomHome) {
            val newRoomHome = RoomHome(
                idRoomHome = editRoomHomeId,
                roomHomeName = binding.nameRoomInput.text.toString(),
                roomHomeDescription = binding.descriptionRoomInput.text.toString(),
                roomHomeImage = newRoomViewModel.imageURI.toString(),
                listDevice = listDeviceRoom,
                dateModify = bufferRoomHome?.dateModify ?: System.currentTimeMillis()
            )
            if (bufferRoomHome != newRoomHome) {
                backFragmentDialog()
            } else {
                findNavController().navigateUp()
            }
        } else {
            findNavController().navigateUp()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            IS_EDIT_ROOM_HOME_DATA, requireArguments().getBoolean(
                IS_EDIT_ROOM_HOME_DATA
            ))
        outState.putLong(EDIT_ROOM_HOME_ID, requireArguments().getLong(EDIT_ROOM_HOME_ID))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Glide.with(requireContext().applicationContext).clear(binding.imageRoom)
        galleryMedia.unregister()
        binding.nameRoomInput.removeTextChangedListener(nameRoomEditTextWatcher)
        onBackPressedCallback.isEnabled = false
        onBackPressedCallback.remove()
        _binding = null
        viewArraySaved = null
        nameRoomEditTextWatcher = null
    }

    companion object {
        private const val DELAY_CLICK = 250L
        private const val IS_EDIT_ROOM_HOME_DATA = "is_edit_room_home_data"
        private const val EDIT_ROOM_HOME_ID = "edit_room_home_id"

        fun createArgsNewRoomHome(): Bundle {
            return bundleOf(
                IS_EDIT_ROOM_HOME_DATA to false
            )
        }

        fun createArgsEditRoomHome(roomHomeId: Long): Bundle {
            return bundleOf(
                IS_EDIT_ROOM_HOME_DATA to true,
                EDIT_ROOM_HOME_ID to roomHomeId
            )
        }
    }
}