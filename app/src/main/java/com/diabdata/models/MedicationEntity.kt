package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey val cipGtin: String,
    val brandName: String,
    val treatmentType: TreatmentType,
    val fullName: String
)
