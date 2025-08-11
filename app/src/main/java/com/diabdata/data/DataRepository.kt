package com.diabdata.data

import com.diabdata.dao.AppointmentDao
import com.diabdata.dao.DiagnosisDateDao
import com.diabdata.dao.HBA1CDao
import com.diabdata.dao.TreatmentDao
import com.diabdata.dao.WeightDao
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry

class DataRepository(
    private val weightDao: WeightDao,
    private val hba1cDao: HBA1CDao,
    private val appointmentDao: AppointmentDao,
    private val treatmentDao: TreatmentDao,
    private val diagnosisDao: DiagnosisDateDao,
) {
    // Weight
    suspend fun insertWeight(weightEntry: WeightEntry) = weightDao.insert(weightEntry)
    suspend fun getAllWeights(): List<WeightEntry> = weightDao.getAllWeights()

    // HBA1C
    suspend fun insertHba1c(hba1cEntry: HBA1CEntry) = hba1cDao.insert(hba1cEntry)
    suspend fun getAllHba1c(): List<HBA1CEntry> = hba1cDao.getAllHBA1C()

    // Appointment
    suspend fun insertAppointment(appointment: Appointment) = appointmentDao.insert(appointment)
    suspend fun getAllAppointments(): List<Appointment> = appointmentDao.getAllAppointments()

    // Treatment
    suspend fun insertTreatment(treatment: Treatment) = treatmentDao.insert(treatment)
    suspend fun getAllTreatments(): List<Treatment> = treatmentDao.getAllTreatments()

    // Diagnosis dates
    suspend fun insertDiagnosisDate(diagnosisDate: DiagnosisDate) = diagnosisDao.insert(diagnosisDate)
    suspend fun getAllDiagnosisDate(): List<DiagnosisDate> = diagnosisDao.getDiagnosisDates()
}
