package com.msaggik.settings.presentation.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.msaggik.settings.databinding.FragmentSettingsBinding
import com.msaggik.settings.presentation.ui.state.StateTheme
import com.msaggik.settings.presentation.view_model.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by viewModel()

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsViewModel.getTheme().value?.let { binding.switchTheme.setChecked(it) }

        binding.switchTheme.setOnCheckedChangeListener { switcher, checked ->

            settingsViewModel.switchTheme(!checked)

            val state = if(checked) StateTheme.Dark else StateTheme.Light
            binding.imageTheme.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    state.stateImageTheme
                )
            )
            binding.textTheme.setText(state.stateText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}