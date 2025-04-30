package io.github.msaggik.home.presentation.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.github.msaggik.home.R
import io.github.msaggik.home.databinding.DialogCreateImageBinding
import io.github.msaggik.home.databinding.FragmentNewLightingModeBinding
import io.github.msaggik.home.domain.model.device.ColorModel
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.util.deleteImageFile
import io.github.msaggik.util.onIsNullOrEmptyChange
import io.github.msaggik.util.refreshImageUri
import io.github.msaggik.util.uriImageForApp
import io.github.msaggik.home.presentation.view_model.NewLightingModeViewModel
import io.github.msaggik.home.presentation.view_model.state.ColorsImageState
import io.github.msaggik.home.presentation.view_model.state.LightingModeState
import io.github.msaggik.util.DebounceMode
import io.github.msaggik.util.convertDpToPx
import io.github.msaggik.util.debounce
import io.github.msaggik.util.showAndHideOthers
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewLightingModeFragment : Fragment() {

    private val newLightingModeViewModel: NewLightingModeViewModel by viewModel()

    private var _binding: FragmentNewLightingModeBinding? = null
    private val binding: FragmentNewLightingModeBinding get() = _binding ?: throw IllegalStateException("Binding should not be called after onDestroy")
    private var viewArrayColors: Array<View>? = null

    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryMedia: ActivityResultLauncher<PickVisualMediaRequest>

    private var isEditLightingMode: Boolean = false
    private var editLightingModeId: Long = -1L
    private var bufferLightingModePoly: LightingModePoly? = null
    private var colors: List<ColorModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            isEditLightingMode = savedInstanceState.getBoolean(IS_EDIT_LIGHTING_MODE_DATA)
            editLightingModeId = savedInstanceState.getLong(EDIT_LIGHTING_MODE_ID)
        } else {
            isEditLightingMode = requireArguments().getBoolean(IS_EDIT_LIGHTING_MODE_DATA)
            editLightingModeId = requireArguments().getLong(EDIT_LIGHTING_MODE_ID)
        }

        cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    drawImage(newLightingModeViewModel.imageCompositionURI)
                    newLightingModeViewModel.setColorsImageLiveData()
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
                newLightingModeViewModel.imageCompositionURI?.let { iUri -> requireContext().deleteImageFile(iUri) }
                newLightingModeViewModel.imageCompositionURI = requireContext().uriImageForApp(uri)
                drawImage(newLightingModeViewModel.imageCompositionURI)
                newLightingModeViewModel.setColorsImageLiveData()
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
                        requireContext().applicationContext.convertDpToPx(16f)
                    )
                )
                .into(binding.imageComposition)
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            newLightingModeViewModel.imageCompositionURI = requireContext().refreshImageUri(newLightingModeViewModel.imageCompositionURI)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, newLightingModeViewModel.imageCompositionURI)
            cameraLauncher.launch(takePictureIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                getString(io.github.msaggik.ui.R.string.permission_to_use_camera_not_granted),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewLightingModeBinding.inflate(inflater, container, false)
        viewArrayColors = arrayOf(
            binding.loadingColors,
            binding.colorsLayout,
            binding.emptyListColors,
            binding.errorMessageColors
        )
        if (isEditLightingMode) {
            binding.header.text = getString(io.github.msaggik.ui.R.string.edit_lighting_mode)
            binding.buttonCreate.text = getString(io.github.msaggik.ui.R.string.edit)
        }
        return binding.root
    }

    private var nameModeEditTextWatcher: TextWatcher? = null

    private lateinit var setLightingModeClickDebounce: (Unit) -> Unit

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLightingModeClickDebounce = viewLifecycleOwner.lifecycleScope.debounce<Unit>(
            delayMillis = DELAY_CLICK,
            mode = DebounceMode.TRAILING
        ) {  createLightingModeData() }

        if (isEditLightingMode) {
            newLightingModeViewModel.getLightingMode(idLightingMode = editLightingModeId)
            newLightingModeViewModel.getLightingModeLiveData().observe(viewLifecycleOwner) {
                renderEditLightingModeViewModel(it)
            }
        }

        drawImage(newLightingModeViewModel.imageCompositionURI)

        newLightingModeViewModel.getColorsImageLiveData().observe(viewLifecycleOwner) {
            renderColorsImage(it)
        }

        binding.imageComposition.setOnClickListener(listener)
        binding.buttonCreate.setOnClickListener(listener)
        binding.buttonBack.setOnClickListener(listener)

        nameModeEditTextWatcher = binding.nameLightingModInput.onIsNullOrEmptyChange { isNullOrEmpty ->
            binding.buttonCreate.isEnabled = !isNullOrEmpty && !colors.isNullOrEmpty()
        }
    }

    private fun renderColorsImage(state: ColorsImageState) {
        when (state) {
            is ColorsImageState.Loading -> {
                binding.loadingColors.showAndHideOthers(viewArrayColors)
            }
            is ColorsImageState.Content -> {
                colors = state.colors
                colors?.let { listColor ->
                    val imageViews = listOf(
                        binding.lightingColorA,
                        binding.lightingColorB,
                        binding.lightingColorC,
                        binding.lightingColorD,
                        binding.lightingColorE
                    )
                    imageViews.forEachIndexed { index, imageView ->
                        if (index < listColor.size) {
                            imageView.imageTintList = convertIntToColorModel(listColor[index])
                        }
                    }
                    binding.colorsLayout.showAndHideOthers(viewArrayColors)
                } ?: run {
                    binding.emptyListColors.showAndHideOthers(viewArrayColors)
                }
            }
            is ColorsImageState.Empty -> {
                binding.emptyListColors.showAndHideOthers(viewArrayColors)
            }
            is ColorsImageState.Error -> {
                binding.errorMessageColors.text = state.errorMessage
                binding.errorMessageColors.showAndHideOthers(viewArrayColors)
            }
        }
    }

    private fun convertIntToColorModel(color: ColorModel): ColorStateList {
        val alpha = 255

        return ColorStateList.valueOf(
            Color.argb(
                alpha,
                color.red,
                color.green,
                color.blue
            )
        )
    }

    private fun renderEditLightingModeViewModel(state: LightingModeState) {
        when (state) {
            is LightingModeState.Loading -> {}
            is LightingModeState.Content -> {
                bufferLightingModePoly = state.lightingModePoly // for equals exit fragment

                binding.nameLightingModInput.setText(state.lightingModePoly.name)

                state.lightingModePoly.uriImage?.let {
                    newLightingModeViewModel.imageCompositionURI = it.toUri()
                    drawImage(it.toUri())
                }
            }
            is LightingModeState.Error -> {}
        }
    }

    private var listener: View.OnClickListener? = View.OnClickListener { p0 ->
        when (p0?.id) {
            R.id.image_composition -> {
                createImageDialog()
            }
            R.id.button_create -> {
                setLightingModeClickDebounce(Unit)
            }
            R.id.button_back -> {
                backFragment()
            }
        }
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

    private fun createLightingModeData() {
        val newLightingModePoly = if(isEditLightingMode && editLightingModeId != -1L) {
            LightingModePoly(
                id = editLightingModeId,
                name = binding.nameLightingModInput.text.toString(),
                uriImage = newLightingModeViewModel.imageCompositionURI.toString(),
                colors = colors ?: listOf(),
                date = System.currentTimeMillis()
            )
        } else {
            LightingModePoly(
                name = binding.nameLightingModInput.text.toString(),
                uriImage = newLightingModeViewModel.imageCompositionURI.toString(),
                colors = colors ?: listOf(),
                date = System.currentTimeMillis()
            )
        }
        newLightingModeViewModel.setLightingMode(newLightingModePoly)
        findNavController().navigateUp()
    }

    private fun backFragment() {
        if (!isEditLightingMode && (newLightingModeViewModel.imageCompositionURI != null
                    || !binding.nameLightingModInput.text.isNullOrEmpty())) {
            backFragmentDialog()
        } else if (isEditLightingMode) {
            val newLightingModePoly = LightingModePoly(
                id = editLightingModeId,
                name = binding.nameLightingModInput.text.toString(),
                uriImage = newLightingModeViewModel.imageCompositionURI.toString(),
                colors = colors ?: listOf(),
                date = bufferLightingModePoly?.date ?: System.currentTimeMillis()
            )
            if (bufferLightingModePoly != newLightingModePoly) {
                backFragmentDialog()
            } else {
                findNavController().navigateUp()
            }
        } else {
            findNavController().navigateUp()
        }
    }

    private fun backFragmentDialog() {
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(getString(io.github.msaggik.ui.R.string.confirm_exit))
            .setMessage(getString(io.github.msaggik.ui.R.string.confirm_exit_description_room))
            .setNeutralButton(getString(io.github.msaggik.ui.R.string.cancel)) { _, _ -> }
            .setPositiveButton(getString(io.github.msaggik.ui.R.string.exit)) { _, _ ->
                newLightingModeViewModel.imageCompositionURI?.let { requireContext().deleteImageFile(it) }
                findNavController().navigateUp()
            }.show()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(
            IS_EDIT_LIGHTING_MODE_DATA, requireArguments().getBoolean(
                IS_EDIT_LIGHTING_MODE_DATA
            ))
        outState.putLong(EDIT_LIGHTING_MODE_ID, requireArguments().getLong(EDIT_LIGHTING_MODE_ID))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Glide.with(requireContext().applicationContext).clear(binding.imageComposition)
        galleryMedia.unregister()
        binding.nameLightingModInput.removeTextChangedListener(nameModeEditTextWatcher)
        _binding = null
        viewArrayColors = null
        nameModeEditTextWatcher = null
    }

    companion object {
        private const val DELAY_CLICK = 250L
        private const val IS_EDIT_LIGHTING_MODE_DATA = "is_edit_lighting_mode_data"
        private const val EDIT_LIGHTING_MODE_ID = "edit_lighting_mode_id"

        fun createArgsNewLightingMode(): Bundle {
            return bundleOf(
                IS_EDIT_LIGHTING_MODE_DATA to false
            )
        }

        fun createArgsEditLightingMode(lightingModeId: Long): Bundle {
            return bundleOf(
                IS_EDIT_LIGHTING_MODE_DATA to true,
                EDIT_LIGHTING_MODE_ID to lightingModeId
            )
        }
    }
}