package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.HBA1CEntry

@Dao
interface HBA1CDao {
    @Insert
    suspend fun insert(hbA1CEntry: HBA1CEntry)

    @Query("SELECT * FROM hba1c_entries ORDER BY date ASC")
    suspend fun getAllHBA1C(): List<HBA1CEntry>
}