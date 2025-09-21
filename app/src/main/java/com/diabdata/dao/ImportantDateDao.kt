package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.ImportantDate
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportantDateDao {
    @Insert
    suspend fun insert(importantDate: ImportantDate)

    @Update
    suspend fun update(importantDate: ImportantDate)

    @Query("UPDATE important_date_entries SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("DELETE FROM important_date_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM important_date_entries WHERE (isArchived = 0 OR isArchived = 1) ORDER BY date DESC")
    fun getAllImportantDates(): Flow<List<ImportantDate>>
}