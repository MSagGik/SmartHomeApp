package io.github.msaggik.home.domain.model.device

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorModel(
    val red: Int,
    val green: Int,
    val blue: Int
): Parcelable