package com.diabdata.models.classes

import com.diabdata.models.AppointmentType
import java.time.LocalDate

data class AppointmentSummary(
    val date: LocalDate,
    val doctor: String,
    val type: AppointmentType,
    val notes: String,
)
