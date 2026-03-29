package com.diabdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.diabdata.core.database.DiabDataDatabase
import com.diabdata.core.model.Appointment
import com.diabdata.feature.appointments.data.AppointmentDao
import com.diabdata.shared.utils.dataTypes.AppointmentType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate

/**
 * Tests for [AppointmentDao] found at [com.diabdata.feature.appointments.data.AppointmentDao]
 */
@RunWith(AndroidJUnit4::class)
class AppointmentDaoTest {
    private lateinit var db: DiabDataDatabase
    private lateinit var appointmentDao: AppointmentDao

    val today: LocalDate = LocalDate.now()

    val appointment = Appointment(
        id = 1,
        date = today.atTime(12, 0),
        doctor = "Dr. Johnson",
        type = AppointmentType.APPOINTMENT,
        createdAt = today,
        isArchived = false,
        notes = "Initial appointment today",
        updatedAt = today
    )
    val appointment2 = appointment.copy(id = 2, date = today.minusMonths(8).plusDays(15).atTime(9, 0), notes = "past appointment", type = AppointmentType.ANNUAL_CHECKUP)
    val appointment3 = appointment.copy(id = 3, date = today.plusMonths(3).plusDays(8).atTime(16, 30), doctor = "Dr. Smith")
    val appointment4 = appointment.copy(id = 4, date = today.plusMonths(3).plusDays(20).atTime(16, 30), doctor = "Dr. Smith", type = AppointmentType.ANNUAL_CHECKUP)


    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            DiabDataDatabase::class.java
        ).build()
        appointmentDao = db.appointmentDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAppointmentTest() = runBlocking {
        appointmentDao.insert(appointment)

        val appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals(appointment, appointments[0])
    }

    @Test
    @Throws(Exception::class)
    fun deleteAppointmentTest() = runBlocking {
        appointmentDao.insert(appointment)

        var appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals(appointment, appointments[0])

        appointmentDao.deleteById(appointment.id)
        appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(0, appointments.size)
    }

    @Test
    @Throws(Exception::class)
    fun updateAppointmentTest() = runBlocking {
        appointmentDao.insert(appointment)

        var appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals(appointment, appointments[0])

        appointmentDao.update(appointment.copy(doctor = "Dr. Smith"))
        appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals("Dr. Smith", appointments[0].doctor)
    }

    @Test
    @Throws(Exception::class)
    fun setAppointmentAsArchivedTest() = runBlocking {
        appointmentDao.insert(appointment)

        var appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals(appointment, appointments[0])

        appointmentDao.setArchived(appointment.id, true)
        appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals(true, appointments[0].isArchived)

        appointmentDao.setArchived(appointment.id, false)
        appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(1, appointments.size)
        assertEquals(false, appointments[0].isArchived)
    }

    @Test
    @Throws(Exception::class)
    fun getAllAppointmentsTest() = runBlocking {
        val allAppointments = listOf(appointment, appointment2, appointment3, appointment4)
        allAppointments.forEach { appointmentDao.insert(it) }

        val appointments = appointmentDao.getAllAppointmentsFlow().first()
        assertEquals(4, appointments.size)
    }

    @Test
    @Throws(Exception::class)
    fun getAllUpcomingAppointmentsTest() = runBlocking {
        val allAppointments = listOf(appointment, appointment2, appointment3, appointment4)
        allAppointments.forEach { appointmentDao.insert(it) }

        val appointments = appointmentDao.getUpcomingAppointmentsFlow(today.atTime(0,0)).first()
        assertEquals(3, appointments.size)
        assertEquals(appointment, appointments[0])
    }
}