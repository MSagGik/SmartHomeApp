package io.github.msaggik.home.presentation.ui.adapters.entity

import androidx.fragment.app.Fragment

data class IotPage(
    val id: Int,
    val titleResId: Int,
    val fragmentCreator: () -> Fragment
)