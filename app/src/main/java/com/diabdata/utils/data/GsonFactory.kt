package com.diabdata.utils.data

import com.diabdata.shared.utils.dataTypes.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDate

object GsonFactory {
    fun create(prettyPrint: Boolean = false): Gson {
        return GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(AppointmentType::class.java, AppointmentTypeAdapter())
            .registerTypeAdapter(DiabetesType::class.java, EnumTypeAdapter(DiabetesType::class.java))
            .registerTypeAdapter(Gender::class.java, EnumTypeAdapter(Gender::class.java))
            .registerTypeAdapter(BloodType::class.java, EnumTypeAdapter(BloodType::class.java))
            .registerTypeAdapter(GlucoseUnit::class.java, EnumTypeAdapter(GlucoseUnit::class.java))
            .apply { if (prettyPrint) setPrettyPrinting() }
            .create()
    }
}