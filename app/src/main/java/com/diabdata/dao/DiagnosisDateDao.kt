package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.DiagnosisDate

@Dao
interface DiagnosisDateDao {
    @Insert
    suspend fun insert(diagnosisDate: DiagnosisDate)
    @Query("SELECT * FROM diagnosis_date_entries ORDER BY date ASC")
    suspend fun getDiagnosisDates(): List<DiagnosisDate>
}