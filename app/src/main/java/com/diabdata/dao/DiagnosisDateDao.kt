package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.DiagnosisDate
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosisDateDao {
    @Insert
    suspend fun insert(diagnosisDate: DiagnosisDate)

    @Update
    suspend fun update(diagnosisDate: DiagnosisDate)

    @Query("UPDATE diagnosis_date_entries SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("DELETE FROM diagnosis_date_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM diagnosis_date_entries ORDER BY date DESC")
    fun getAllDiagnosisDatesFlow(): Flow<List<DiagnosisDate>>
}