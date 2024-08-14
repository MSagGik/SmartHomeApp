package com.msaggik.settings.presentation.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.msaggik.common_ui.R
import com.msaggik.settings.databinding.FragmentSettingsBinding
import com.msaggik.settings.presentation.ui.adapters.LanguageAdapter
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

        val isChecked = if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            true
        } else {
            false
        }
        val state = if(isChecked) StateTheme.Dark else StateTheme.Light
        binding.switchTheme.isChecked = isChecked
        binding.imageTheme.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                state.stateImageTheme
            )
        )
        binding.textTheme.setText(state.stateText)

        binding.switchTheme.setOnCheckedChangeListener { switcher, checked ->
            settingsViewModel.switchTheme(!checked)
        }

        // language settings
        val languageValues = resources.getStringArray(R.array.language_selection_values)
        val languageSelectionAdapter = LanguageAdapter(
            view.context,
            resources.getIntArray(R.array.language_selection),
            resources.getStringArray(R.array.language_selection)
        )
        binding.languageSelection.adapter = languageSelectionAdapter

        settingsViewModel.getLanguage().observe(viewLifecycleOwner) { language ->
            binding.languageSelection.setSelection(languageValues.indexOf(language))
            languageSelectionAdapter.notifyDataSetChanged()
        }

        binding.languageSelection.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                settingsViewModel.setLanguage(languageValues[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}