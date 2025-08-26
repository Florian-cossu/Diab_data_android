package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "treatments")
data class Treatment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val expirationDate: LocalDate,
    val name: String,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val type: TreatmentType
)