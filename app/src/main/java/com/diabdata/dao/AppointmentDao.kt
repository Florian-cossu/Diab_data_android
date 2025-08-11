package com.diabdata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.diabdata.models.Appointment

@Dao
interface AppointmentDao {
    @Insert
    suspend fun insert(appointment: Appointment)

    @Query("SELECT * FROM appointments ORDER BY date ASC")
    suspend fun getAllAppointments(): List<Appointment>
}