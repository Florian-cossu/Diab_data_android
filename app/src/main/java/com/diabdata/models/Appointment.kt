package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val doctor: String,
    val type: AppointmentType,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val notes: String?
)
