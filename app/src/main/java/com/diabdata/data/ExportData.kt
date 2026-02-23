package com.diabdata.data

import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.Treatment
import com.diabdata.models.UserDetails
import com.diabdata.models.WeightEntry
import kotlinx.coroutines.flow.Flow

data class ExportData(
    val weights: List<WeightEntry> = emptyList(),
    val hba1c: List<HBA1CEntry> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val treatments: List<Treatment> = emptyList(),
    val importantDates: List<ImportantDate> = emptyList(),
    val devices: List<MedicalDeviceEntry> = emptyList(),
    val userDetails: UserDetails? = null
)