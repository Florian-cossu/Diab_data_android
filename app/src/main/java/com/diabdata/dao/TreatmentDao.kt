package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.Treatment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TreatmentDao {
    @Insert
    suspend fun insert(treatment: Treatment)

    @Update
    suspend fun update(treatment: Treatment)

    @Query("DELETE FROM treatments WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE treatments SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("SELECT * FROM treatments WHERE (isArchived = 0 OR isArchived = 1) ORDER BY expirationDate DESC")
    fun getAllTreatmentsFlow(): Flow<List<Treatment>>

    @Query("SELECT * FROM treatments WHERE expirationDate >= :today AND isArchived = 0 ORDER BY type, expirationDate ASC")
    fun getUpcomingExpirationDatesFlow(today: LocalDate): Flow<List<Treatment>>
}