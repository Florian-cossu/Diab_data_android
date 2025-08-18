package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.Appointment
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface AppointmentDao {
    // Legacy
    @Insert
    suspend fun insert(appointment: Appointment)

    @Query("SELECT * FROM appointments ORDER BY date ASC")
    suspend fun getAllAppointments(): List<Appointment>

    // Updated flow version
    @Query("SELECT * FROM appointments ORDER BY date DESC")
    fun getAllAppointmentsFlow(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE date >= :today ORDER BY date ASC")
    fun getUpcomingAppointmentsFlow(today: LocalDate = LocalDate.now()): Flow<List<Appointment>>

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)
}
