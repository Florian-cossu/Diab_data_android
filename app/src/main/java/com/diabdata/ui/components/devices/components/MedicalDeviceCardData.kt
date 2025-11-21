package com.diabdata.ui.components.devices.components

import androidx.compose.ui.graphics.Color
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.shared.utils.dataTypes.AddableType
import java.time.LocalDate

data class MedicalDeviceCardData(
    val device: MedicalDeviceEntry,
    val titleText: String,
    val textColor: Color,
    val addableType: AddableType,
    val isLifeSpanOver: Boolean,
    val iconRes: Int,
    val date: LocalDate,
    val lifeSpanEndDate: LocalDate,
    val lifeSpan: Int,
    val batchNumber: String,
)