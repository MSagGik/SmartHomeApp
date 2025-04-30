package io.github.msaggik.home.domain.model.device

import io.github.msaggik.home.R
import org.json.JSONObject

data class ClimateUIModel(
    val validData: Boolean,
    val temperature: Float,
    val tempIconRes: Int,
    val humidity: Float,
    val humidityIconRes: Int,
    val pressure: Float,
    val pressureIconRes: Int
) {
    companion object {
        private const val TYPE_JSON = "SMART_HOME_DATA"
        private const val MIN_TEMPERATURE = 18
        private const val MAX_TEMPERATURE = 24
        private const val MIN_HUMIDITY = 40
        private const val MAX_HUMIDITY = 60
        private const val MIN_PRESSURE = 747
        private const val MAX_PRESSURE = 760

        fun defaultClimateUIModel(): ClimateUIModel {
            return ClimateUIModel(
                validData = false,
                temperature = 0f,
                humidity = 0f,
                pressure = 0f,
                tempIconRes = R.drawable.ic_device_temp,
                humidityIconRes = R.drawable.ic_device_humidity,
                pressureIconRes = R.drawable.ic_device_pressure
            )
        }

        fun climateDataConvert(rawJson: String): ClimateUIModel? {
            return try {
                val json = JSONObject(rawJson)
                val type = json.getString("type")
                if (type == TYPE_JSON) {
                    val temp = json.getDouble("temperature").toFloat()
                    val humidity = json.getDouble("humidity").toFloat()
                    val pressure = json.getDouble("pressure").toFloat()

                    ClimateUIModel(
                        validData = true,
                        temperature = temp,
                        humidity = humidity,
                        pressure = pressure,
                        tempIconRes = interpretTempIcon(temp),
                        humidityIconRes = interpretHumidityIcon(humidity),
                        pressureIconRes = interpretPressureIcon(pressure)
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                defaultClimateUIModel()
            }
        }

        private fun interpretTempIcon(value: Float): Int {
            return when {
                value < MIN_TEMPERATURE -> R.drawable.ic_sensor_temp_low
                value > MAX_TEMPERATURE -> R.drawable.ic_sensor_temp_high
                else -> R.drawable.ic_sensor_temp_normal
            }
        }

        private fun interpretHumidityIcon(value: Float): Int {
            return when {
                value < MIN_HUMIDITY -> R.drawable.ic_sensor_humidity_low
                value > MAX_HUMIDITY -> R.drawable.ic_sensor_humidity_high
                else -> R.drawable.ic_sensor_humidity_normal
            }
        }

        private fun interpretPressureIcon(value: Float): Int {
            return when {
                value < MIN_PRESSURE -> R.drawable.ic_sensor_pressure_low
                value > MAX_PRESSURE -> R.drawable.ic_sensor_pressure_high
                else -> R.drawable.ic_sensor_pressure_normal
            }
        }
    }
}