package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.HBA1CEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HBA1CDao {
    // Legacy
    @Insert
    suspend fun insert(hba1cEntry: HBA1CEntry)

    @Query("SELECT * FROM hba1c_entries ORDER BY date DESC")
    fun getAllHBA1CFlow(): Flow<List<HBA1CEntry>>

    @Query("SELECT * FROM hba1c_entries WHERE date >= :minDate ORDER BY date DESC")
    fun getHBA1CEntriesSince(minDate: LocalDate): Flow<List<HBA1CEntry>>

    @Delete
    suspend fun deleteHBA1CEntry(hba1cEntry: HBA1CEntry)
}
