package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.Treatment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TreatmentDao {
    @Insert
    suspend fun insert(treatment: Treatment)

    @Query("DELETE FROM treatments WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM treatments ORDER BY expirationDate DESC")
    fun getAllTreatmentsFlow(): Flow<List<Treatment>>

    @Query("SELECT * FROM treatments WHERE expirationDate >= :today ORDER BY expirationDate ASC")
    fun getUpcomingExpirationDatesFlow(today: LocalDate = LocalDate.now()): Flow<List<Treatment>>
}