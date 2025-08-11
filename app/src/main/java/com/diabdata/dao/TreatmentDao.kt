package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.Treatment

@Dao
interface TreatmentDao {
    @Insert
    suspend fun insert(treatment: Treatment)

    @Query("SELECT * FROM treatments ORDER BY expirationDate ASC")
    suspend fun getAllTreatments(): List<Treatment>
}