package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeightDao {
    // Legacy
    @Insert
    suspend fun insert(weightEntry: WeightEntry)

    @Query("SELECT * FROM weight_entries ORDER BY date ASC")
    suspend fun getAllWeights(): List<WeightEntry>

    // Flow versions
    @Query("SELECT * FROM weight_entries ORDER BY date DESC")
    fun getAllWeightsFlow(): Flow<List<WeightEntry>>

    // Dernières valeurs depuis un an (pour les tendances)
    @Query("SELECT * FROM weight_entries WHERE date >= :minDate ORDER BY date DESC")
    fun getWeightsSince(minDate: LocalDate): Flow<List<WeightEntry>>

    @Delete
    suspend fun deleteWeightEntry(weightEntry: WeightEntry)
}
