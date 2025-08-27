package com.diabdata.data

import com.diabdata.dao.AppointmentDao
import com.diabdata.dao.DiagnosisDateDao
import com.diabdata.dao.HBA1CDao
import com.diabdata.dao.MedicationDao
import com.diabdata.dao.TreatmentDao
import com.diabdata.dao.WeightDao
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.MedicationEntity
import com.diabdata.models.PlotPoint
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class DataRepository(
    private val weightDao: WeightDao,
    private val hba1cDao: HBA1CDao,
    private val appointmentDao: AppointmentDao,
    private val treatmentDao: TreatmentDao,
    private val diagnosisDao: DiagnosisDateDao,
    val database: DiabDataDatabase,
) {
    private val medicationDao: MedicationDao = database.medicationDao()

    // ----------------
    // Weight
    // ----------------
    /** Insert a new weight entry */
    suspend fun insertWeight(weightEntry: WeightEntry) = weightDao.insert(weightEntry)

    /** Update weight entry */
    suspend fun updateWeight(weightEntry: WeightEntry) = weightDao.update(weightEntry)

    /** Flow of all weight entries, sorted by date */
    fun getAllWeightsFlow(): Flow<List<WeightEntry>> = weightDao.getAllWeightsFlow()

    /** Flow of weight points to plot in graph between min and max date */
    fun getWeightPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        weightDao.getWeightPlotData(minDate, maxDate)

    /** Flow of weight entries from the last year */
    fun getRecentWeightsFlow(): Flow<List<WeightEntry>> {
        val oneYearAgo = LocalDate.now().minusYears(1)
        return weightDao.getWeightsSince(oneYearAgo)
    }

    /** Delete a weight record by Id**/
    suspend fun deleteWeight(id: Int) = weightDao.deleteById(id)

    // ----------------
    // HBA1C
    // ----------------
    /** Insert a new HBA1C entry */
    suspend fun insertHba1c(hba1cEntry: HBA1CEntry) = hba1cDao.insert(hba1cEntry)

    /** Update HBA1C */
    suspend fun updateHBA1C(hba1cEntry: HBA1CEntry) = hba1cDao.update(hba1cEntry)

    /** Flow of all HBA1C entries */
    fun getAllHba1cFlow(): Flow<List<HBA1CEntry>> = hba1cDao.getAllHBA1CFlow()

    /** Flow of weight points to plot in graph between min and max date */
    fun getHba1cPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        hba1cDao.getHBA1CPlotData(minDate, maxDate)

    /** Flow of HBA1C entries from the last year */
    fun getRecentHba1cFlow(): Flow<List<HBA1CEntry>> {
        val oneYearAgo = LocalDate.now().minusYears(1)
        return hba1cDao.getHBA1CEntriesSince(oneYearAgo)
    }

    /** Delete an HBA1C record by Id**/
    suspend fun deleteHba1c(id: Int) = hba1cDao.deleteById(id)

    // ----------------
    // Appointment
    // ----------------
    /** Insert a new appointment */
    suspend fun insertAppointment(appointment: Appointment) = appointmentDao.insert(appointment)

    /** Update appointment */
    suspend fun updateAppointment(appointment: Appointment) = appointmentDao.update(appointment)

    /** Flow of all appointments */
    fun getAllAppointmentsFlow(): Flow<List<Appointment>> = appointmentDao.getAllAppointmentsFlow()

    /** Flow of upcoming appointments starting today */
    fun getUpcomingAppointments(): Flow<List<Appointment>> {
        return appointmentDao.getUpcomingAppointmentsFlow(LocalDate.now())
    }

    /** Delete an Appointment record by Id**/
    suspend fun deleteAppointment(id: Int) = appointmentDao.deleteById(id)

    // ----------------
    // Treatment
    // ----------------
    /** Insert a new treatment */
    suspend fun insertTreatment(treatment: Treatment) = treatmentDao.insert(treatment)

    /** Update treatment */
    suspend fun updateTreatment(treatment: Treatment) = treatmentDao.update(treatment)

    /** Flow of all treatments */
    fun getAllTreatmentsFlow(): Flow<List<Treatment>> = treatmentDao.getAllTreatmentsFlow()

    /** Flow of upcoming treatments expiration dates */
    fun getUpcomingExpDates(date: LocalDate): Flow<List<Treatment>> =
        treatmentDao.getUpcomingExpirationDatesFlow(LocalDate.now())

    /** Delete a treatment record by Id**/
    suspend fun deleteTreatment(id: Int) = treatmentDao.deleteById(id)

    // ----------------
    // Medication
    // ----------------
    /** Find a medication by its code (GTIN or CIP) */
    suspend fun findMedicationByCode(code: String): MedicationEntity? =
        medicationDao.findByCode(code)

    // ----------------
    // Diagnosis Dates
    // ----------------
    /** Insert a new diagnosis date */
    suspend fun insertDiagnosisDate(diagnosisDate: DiagnosisDate) = diagnosisDao.insert(diagnosisDate)

    /** Update diagnosis date */
    suspend fun updateDiagnosisDate(diagnosisDate: DiagnosisDate) =
        diagnosisDao.update(diagnosisDate)

    /** Archive appointment */
    suspend fun setArchivedDiagnosisDate(id: Int, archived: Boolean) =
        diagnosisDao.setArchived(id, archived)

    /** Flow of all diagnosis dates */
    fun getAllDiagnosisDatesFlow(): Flow<List<DiagnosisDate>> =
        diagnosisDao.getAllDiagnosisDatesFlow()

    /** Delete a diagnosis date record by Id**/
    suspend fun deleteDiagnosis(id: Int) = diagnosisDao.deleteById(id)

    // ----------------
    // Generic / Database Utilities
    // ----------------
    /** Clear the entire database and reset autoincrement IDs */
    fun clearAllDataAndReset() = database.clearAllDataAndReset()
}