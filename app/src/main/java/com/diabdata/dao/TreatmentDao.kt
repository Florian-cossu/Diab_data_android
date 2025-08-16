package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.Treatment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TreatmentDao {
    // Legacy
    @Insert
    suspend fun insert(treatment: Treatment)

    @Query("SELECT * FROM treatments ORDER BY expirationDate ASC")
    suspend fun getAllTreatments(): List<Treatment>

    // Updated flow version
    @Query("SELECT * FROM treatments ORDER BY expirationDate DESC")
    fun getAllExpirationDatesFlow(): Flow<List<Treatment>>

    @Query("SELECT * FROM treatments WHERE expirationDate >= :today ORDER BY expirationDate ASC")
    fun getUpcomingExpirationDatesFlow(today: LocalDate = LocalDate.now()): Flow<List<Treatment>>

    @Delete
    suspend fun deleteExpirationDate(treatment: Treatment)
}