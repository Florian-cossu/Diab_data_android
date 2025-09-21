package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medical_devices_infos")
data class MedicalDeviceInfoEntity(
    @PrimaryKey val cipGtin: String,
    val manufacturer: String,
    val deviceType: MedicalDeviceInfoType,
    val fullName: String,
    val daysLifespan: Int
)