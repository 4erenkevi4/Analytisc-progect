package com.cher.analytics.data

import androidx.room.TypeConverter
import com.github.instagram4j.instagram4j.models.user.Profile
import com.google.gson.Gson

class Converter {

        @TypeConverter
        fun listToJsonString(value: List<FormattedFollowers>): String = Gson().toJson(value)

        @TypeConverter
        fun jsonStringToList(value: String) = Gson().fromJson(value, Array<FormattedFollowers>::class.java).toList()
    }