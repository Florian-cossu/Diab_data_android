package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.diabdata.models.Appointment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface AppointmentDao {
    @Insert
    suspend fun insert(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Update
    suspend fun update(appointment: Appointment)

    @Query("UPDATE appointments SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: Int, archived: Boolean)

    @Query("SELECT * FROM appointments WHERE (isArchived = 0 OR isArchived = 1) ORDER BY date DESC")
    fun getAllAppointmentsFlow(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date >= :today AND isArchived = 0 ORDER BY date ASC")
    fun getUpcomingAppointmentsFlow(today: LocalDateTime): Flow<List<Appointment>>
}
