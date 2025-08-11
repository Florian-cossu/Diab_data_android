package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.WeightEntry

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weightEntry: WeightEntry)

    @Query("SELECT * FROM weight_entries ORDER BY date ASC")
    suspend fun getAllWeights(): List<WeightEntry>
}