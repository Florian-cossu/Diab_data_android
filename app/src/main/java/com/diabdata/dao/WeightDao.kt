package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weightEntry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllWeightsFlow(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries WHERE date >= :minDate ORDER BY date DESC")
    fun getWeightsSince(minDate: LocalDate): Flow<List<WeightEntry>>
}
