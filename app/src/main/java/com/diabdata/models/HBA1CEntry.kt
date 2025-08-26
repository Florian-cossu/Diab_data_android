package com.diabdata.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "hba1c_entries")
data class HBA1CEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: LocalDate,
    val createdAt: LocalDate,
    val isArchived: Boolean,
    val value: Float,
)