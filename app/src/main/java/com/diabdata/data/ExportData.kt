package com.diabdata.data

import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry

data class ExportData(
    val weights: List<WeightEntry> = emptyList(),
    val hba1c: List<HBA1CEntry> = emptyList(),
    val appointments: List<Appointment> = emptyList(),
    val treatments: List<Treatment> = emptyList(),
    val importantDates: List<ImportantDate> = emptyList()
)