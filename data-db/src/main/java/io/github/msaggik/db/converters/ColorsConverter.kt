package io.github.msaggik.db.converters

import androidx.room.TypeConverter

object ColorsConverter {
    @TypeConverter
    fun fromColors(colors: List<Int>): String =
        colors.joinToString(",")

    @TypeConverter
    fun toColors(data: String): List<Int> =
        if (data.isEmpty()) emptyList()
        else data.split(",").map { it.toInt() }
}