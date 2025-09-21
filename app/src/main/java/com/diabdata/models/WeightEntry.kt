package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val value: Float,
    val updatedAt: LocalDate,
)