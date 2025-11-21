package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.classes.FaultyBatchCount
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
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

    @Query("SELECT * FROM medical_devices WHERE (isArchived = 0 OR isArchived = 1) AND (deviceType != 'WIRELESS_PATCH_REMOTE' OR deviceType != 'WIRED_PUMP') ORDER BY date DESC")
    fun getAllConsumableDevices(): Flow<List<MedicalDeviceEntry>>

    @Query("SELECT * FROM medical_devices WHERE (isArchived = 0 OR isArchived = 1) AND (deviceType = 'WIRELESS_PATCH_REMOTE' OR deviceType = 'WIRED_PUMP') ORDER BY date DESC")
    fun getAllNonConsumableDevices(): Flow<List<MedicalDeviceEntry>>

    @Query("SELECT * FROM medical_devices WHERE (isArchived = 0 OR isArchived = 1) AND (lifeSpanEndDate >= :today AND isFaulty = 0) AND isLifeSpanOver = 0 AND (deviceType != 'WIRELESS_PATCH_REMOTE' AND deviceType != 'WIRED_PUMP')")
    fun getAllCurrentConsumableMedicalDevices(today: LocalDate): Flow<List<MedicalDeviceEntry>>

    @Query("SELECT * FROM medical_devices WHERE isFaulty=1 AND isReported=0")
    fun getAllFaultyUnreportedMedicalDevices(): Flow<List<MedicalDeviceEntry>>

    @Query("SELECT * FROM medical_devices WHERE isFaulty=1 AND isReported=1")
    fun getAllFaultyReportedMedicalDevices(): Flow<List<MedicalDeviceEntry>>

    @Query(
        """
    SELECT COUNT(*) AS count, batchNumber 
    FROM medical_devices 
    WHERE isFaulty = 1 AND isReported = 1 
    GROUP BY batchNumber 
    ORDER BY count DESC
"""
    )
    fun getFaultyBatchNumbersCounts(): Flow<List<FaultyBatchCount>>

    @Query("SELECT * from medical_devices WHERE deviceType = :deviceType AND lifeSpanEndDate <= :today")
    fun getSimilarExpiringConsumableDevices(
        today: LocalDate,
        deviceType: MedicalDeviceInfoType
    ): List<MedicalDeviceEntry>

    @Query("SELECT * FROM medical_devices WHERE :expirationDate >= :today AND isArchived = 0 ORDER BY deviceType, date ASC")
    fun getUpcomingExpirationDatesFlow(
        today: LocalDate,
        expirationDate: LocalDate
    ): Flow<List<MedicalDeviceEntry>>
}