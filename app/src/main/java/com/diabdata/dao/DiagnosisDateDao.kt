package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.DiagnosisDate
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosisDateDao {
    @Insert
    suspend fun insert(diagnosisDate: DiagnosisDate)

    @Query("DELETE FROM diagnosis_date_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM diagnosis_date_entries ORDER BY date ASC")
    fun getAllDiagnosisDatesFlow(): Flow<List<DiagnosisDate>>
}