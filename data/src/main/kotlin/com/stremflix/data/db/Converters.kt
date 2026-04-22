package com.stremflix.data.db

import androidx.room.TypeConverter
import com.stremflix.core.model.MediaType

class Converters {
    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = MediaType.valueOf(value)
}
