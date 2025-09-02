package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "important_date_entries")
data class ImportantDate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val importantDate: String,
    val updatedAt: LocalDate,
)