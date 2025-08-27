package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.PlotPoint
import com.diabdata.models.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(weightEntry: WeightEntry)

    @Update
    suspend fun update(weightEntry: WeightEntry)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE weight_entries SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("SELECT * FROM weight_entries WHERE (isArchived = 0 OR isArchived = 1) ORDER BY date DESC")
    fun getAllWeightsFlow(): Flow<List<WeightEntry>>

    @Query("SELECT value,date FROM weight_entries WHERE (date >= :minDate AND date <= :maxDate) AND isArchived = 0 ORDER BY date ASC ")
    fun getWeightPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>>

    @Query("SELECT * FROM weight_entries WHERE date >= :minDate AND isArchived = 0 ORDER BY date DESC")
    fun getWeightsSince(minDate: LocalDate): Flow<List<WeightEntry>>
}
