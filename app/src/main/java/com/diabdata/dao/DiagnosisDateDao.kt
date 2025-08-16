package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.DiagnosisDate
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosisDateDao {
    //legacy
    @Insert
    suspend fun insert(diagnosisDate: DiagnosisDate)
    @Query("SELECT * FROM diagnosis_date_entries ORDER BY date ASC")
    suspend fun getDiagnosisDates(): List<DiagnosisDate>

    // Updated flow version
    @Query("SELECT * FROM diagnosis_date_entries ORDER BY date ASC")
    fun getAllDiagnosisFlow(): Flow<List<DiagnosisDate>>
}