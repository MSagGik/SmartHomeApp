package io.github.msaggik.home.domain.model.device

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LightingModePoly(
    val id: Long = 0L,
    val name: String,
    val uriImage: String?,
    val colors: List<ColorModel>,
    val date: Long = -1L
): Parcelable {
    companion object {
        private const val NUMBER_COLORS_UI_MODE = 5
        private const val NULL_LIGHTING_UI_MODE = 0

        fun defaultNullLightingMode(): LightingModePoly {
            return LightingModePoly(
                name = "",
                uriImage = null,
                colors = List(NUMBER_COLORS_UI_MODE) { ColorModel(NULL_LIGHTING_UI_MODE, NULL_LIGHTING_UI_MODE, NULL_LIGHTING_UI_MODE) }
            )
        }

        fun defaultStartLightingMode(startValue: Int): LightingModePoly {
            return LightingModePoly(
                name = "",
                uriImage = null,
                colors = List(NUMBER_COLORS_UI_MODE) { ColorModel(startValue, startValue, startValue) }
            )
        }
    }
}
