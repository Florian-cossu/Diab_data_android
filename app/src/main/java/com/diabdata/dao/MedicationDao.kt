package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.diabdata.models.MedicationEntity

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE cipGtin = :code LIMIT 1")
    suspend fun findByCode(code: String): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(medications: List<MedicationEntity>)
}
