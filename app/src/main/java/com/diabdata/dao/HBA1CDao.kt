package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.HBA1CEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HBA1CDao {
    // Legacy
    @Insert
    suspend fun insert(hba1cEntry: HBA1CEntry)

    @Update
    suspend fun update(hba1cEntry: HBA1CEntry)

    @Query("DELETE FROM hba1c_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE hba1c_entries SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("SELECT * FROM hba1c_entries ORDER BY date DESC")
    fun getAllHBA1CFlow(): Flow<List<HBA1CEntry>>

    @Query("SELECT * FROM hba1c_entries WHERE date >= :minDate AND isArchived = 0 ORDER BY date DESC")
    fun getHBA1CEntriesSince(minDate: LocalDate): Flow<List<HBA1CEntry>>
}
