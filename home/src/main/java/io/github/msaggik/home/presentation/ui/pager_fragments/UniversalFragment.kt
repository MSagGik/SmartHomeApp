package io.github.msaggik.home.presentation.ui.pager_fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import io.github.msaggik.home.databinding.FragmentUniversalBinding
import io.github.msaggik.home.presentation.view_model.pager_view_model.UniversalViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class UniversalFragment : Fragment() {
    private val universalViewModel: UniversalViewModel by viewModel()

    private var _binding: FragmentUniversalBinding? = null
    private val binding: FragmentUniversalBinding
        get() = _binding
            ?: throw IllegalStateException("Binding should not be called after onDestroy")

    private var macAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        macAddress = savedInstanceState?.getString(MAC_ADDRESS_DEVICE) ?: requireArguments().getString(
            MAC_ADDRESS_DEVICE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUniversalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "onViewCreated $TAG")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(MAC_ADDRESS_DEVICE, requireArguments().getString(MAC_ADDRESS_DEVICE))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.e(TAG, "onDestroyView $TAG")
    }

    companion object {
        private const val TAG = "UniversalFragment"
        private const val MAC_ADDRESS_DEVICE = "mac_address_device"
        fun newInstance(macAddress: String): UniversalFragment {
            return UniversalFragment().apply {
                arguments = bundleOf(MAC_ADDRESS_DEVICE to macAddress)
            }
        }
    }
}