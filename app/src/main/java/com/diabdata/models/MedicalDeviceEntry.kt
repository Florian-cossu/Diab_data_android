package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medical_devices")
data class MedicalDevice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val batchNumber: String,
    val serialNumber: String?,
    val manufacturer: String?,
    val type: MedicalDevicesTypes,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val updatedAt: LocalDate
)