package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "diagnosis_date_entries")
data class DiagnosisDate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val diagnosis: String,
)