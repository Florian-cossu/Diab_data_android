package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.MedicalDeviceEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MedicalDeviceDao {
    @Insert
    suspend fun insert(device: MedicalDeviceEntry)

    @Update
    suspend fun update(device: MedicalDeviceEntry)

    @Query("DELETE FROM medical_devices WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM medical_devices WHERE (isArchived = 0 OR isArchived = 1) ORDER BY date DESC")
    fun getAllMedicalDevices(): Flow<List<MedicalDeviceEntry>>

    @Query("SELECT * FROM medical_devices WHERE :expirationDate >= :today AND isArchived = 0 ORDER BY deviceType, date ASC")
    fun getUpcomingExpirationDatesFlow(
        today: LocalDate,
        expirationDate: LocalDate
    ): Flow<List<MedicalDeviceEntry>>

}