package io.github.msaggik.home.domain.model.device

import android.util.Log
import org.json.JSONObject

data class LightingUIModel(
    val validData: Boolean,
    val macAddress: String,
    var mode: Long,
    var alpha: Int,
    var colors: List<ColorModel>
) {
    companion object {
        private const val TAG = "LightingUIModel"
        private const val TYPE_JSON = "SMART_HOME_DATA"
        private const val NUMBER_COLORS_UI_MODE = 5
        private const val NULL_LIGHTING_UI_MODE = 0
        private const val START_LIGHTING_UI_MODE = 25

        fun defaultNullLightingUIModel(macAddress: String): LightingUIModel {
            return LightingUIModel(
                validData = false,
                macAddress = macAddress,
                mode = 0,
                alpha = NULL_LIGHTING_UI_MODE,
                colors = List(NUMBER_COLORS_UI_MODE) { ColorModel(NULL_LIGHTING_UI_MODE, NULL_LIGHTING_UI_MODE, NULL_LIGHTING_UI_MODE) }
            )
        }

        fun defaultStartLightingUIModel(macAddress: String): LightingUIModel {
            return LightingUIModel(
                validData = true,
                macAddress = macAddress,
                mode = 0,
                alpha = START_LIGHTING_UI_MODE,
                colors = List(NUMBER_COLORS_UI_MODE) { ColorModel(START_LIGHTING_UI_MODE, START_LIGHTING_UI_MODE, START_LIGHTING_UI_MODE) }
            )
        }

        fun jsonToLightingUIModelConvert(macAddress: String, rawJson: String): LightingUIModel? {
            return try {
                val json = JSONObject(rawJson)
                val type = json.getString("type")
                if (type == TYPE_JSON) {
                    val mode = json.getDouble("led_m").toLong()
                    val alpha = json.getDouble("led_a").toInt()

                    val colors = if (mode >= 0L) {
                        val color = ColorModel(
                            red = json.getDouble("led_r").toInt(),
                            green = json.getDouble("led_g").toInt(),
                            blue = json.getDouble("led_b").toInt()
                        )
                        List(NUMBER_COLORS_UI_MODE) { color }
                    } else {
                        return null
                    }

                    LightingUIModel(
                        validData = true,
                        macAddress = macAddress,
                        mode = mode,
                        alpha = alpha,
                        colors = colors
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "[jsonToLightingUIModelConvert] receiveData to macAddress $macAddress error ${e.message}")
                null
            }
        }

        fun lightingDataAlphaToByteArrayConvert(lightingUIModel: LightingUIModel): ByteArray {
            val jsonLightingUIModelAlpha = JSONObject().apply {
                put("type", "LED_ALPHA")
                put("a", lightingUIModel.alpha)
            }.toString() + "\n"
            return jsonLightingUIModelAlpha.toByteArray(Charsets.UTF_8)
        }

        fun lightingDataMonoToByteArrayConvert(lightingUIModel: LightingUIModel): ByteArray {
            val jsonLightingUIModelBase = JSONObject().apply {
                put("type", "LED_MONO")
                put("m", lightingUIModel.mode)
                put("a", lightingUIModel.alpha)
                put("r", lightingUIModel.colors[0].red)
                put("g", lightingUIModel.colors[0].green)
                put("b", lightingUIModel.colors[0].blue)
            }.toString() + "\n"
            return jsonLightingUIModelBase.toByteArray(Charsets.UTF_8)
        }

        fun lightingListDataToByteArrayConvert(lightingUIModel: LightingUIModel): List<ByteArray> {
            val listByteArray = mutableListOf<ByteArray>()

            val jsonLightingUIModelFlowA = JSONObject().apply {
                put("type", "LED_FLOW_A")
                put("a", lightingUIModel.alpha)
                put("r0", lightingUIModel.colors[0].red)
                put("g0", lightingUIModel.colors[0].green)
                put("b0", lightingUIModel.colors[0].blue)
                put("r1", lightingUIModel.colors[1].red)
            }.toString() + "\n"
            listByteArray.add(jsonLightingUIModelFlowA.toByteArray(Charsets.UTF_8))

            val jsonLightingUIModelFlowB = JSONObject().apply {
                put("type", "LED_FLOW_B")
                put("g1", lightingUIModel.colors[1].green)
                put("b1", lightingUIModel.colors[1].blue)
                put("r2", lightingUIModel.colors[2].red)
                put("g2", lightingUIModel.colors[2].green)
                put("b2", lightingUIModel.colors[2].blue)
            }.toString() + "\n"
            listByteArray.add(jsonLightingUIModelFlowB.toByteArray(Charsets.UTF_8))

            val jsonLightingUIModelFlowC = JSONObject().apply {
                put("type", "LED_FLOW_C")
                put("r3", lightingUIModel.colors[3].red)
                put("g3", lightingUIModel.colors[3].green)
                put("b3", lightingUIModel.colors[3].blue)
                put("r4", lightingUIModel.colors[4].red)
                put("g4", lightingUIModel.colors[4].green)
            }.toString() + "\n"
            listByteArray.add(jsonLightingUIModelFlowC.toByteArray(Charsets.UTF_8))
            return listByteArray
        }

        fun lightingStartListDataToByteArrayConvert(lightingUIModel: LightingUIModel): ByteArray {
            val jsonLightingUIModelFlowFinal = JSONObject().apply {
                put("type", "LED_FLOW_FIN")
                put("b4", lightingUIModel.colors[4].blue)
                put("m", lightingUIModel.mode)
            }.toString() + "\n"
            return jsonLightingUIModelFlowFinal.toByteArray(Charsets.UTF_8)
        }
    }
}
