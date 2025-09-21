package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medical_devices")
data class MedicalDeviceEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val name: String,
    val batchNumber: String,
    val serialNumber: String?,
    val manufacturer: String?,
    val deviceType: MedicalDeviceInfoType,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val lifeSpan: Int,
    val isFaulty: Boolean,
    val isReported: Boolean,
    val isLifeSpanOver: Boolean,
    val updatedAt: LocalDate
)