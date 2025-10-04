package com.diabdata.data

import com.diabdata.dao.AppointmentDao
import com.diabdata.dao.HBA1CDao
import com.diabdata.dao.ImportantDateDao
import com.diabdata.dao.MedicalDeviceDao
import com.diabdata.dao.MedicalDevicesInfoDao
import com.diabdata.dao.MedicationDao
import com.diabdata.dao.TreatmentDao
import com.diabdata.dao.WeightDao
import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.MedicalDeviceInfoEntity
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.models.classes.FaultyBatchCount
import com.diabdata.models.classes.PlotPoint
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class DataRepository(
    private val weightDao: WeightDao,
    private val hba1cDao: HBA1CDao,
    private val appointmentDao: AppointmentDao,
    private val treatmentDao: TreatmentDao,
    private val importantDateDao: ImportantDateDao,
    private val medicationDao: MedicationDao,
    private val medicalDevicesDao: MedicalDeviceDao,
    private val medicalDeviceInfo: MedicalDevicesInfoDao,
    val database: DiabDataDatabase,
) {
    // ----------------
    // Weight
    // ----------------
    /** Insert a new weight entry */
    suspend fun insertWeight(weightEntry: WeightEntry) = weightDao.insert(weightEntry)

    /** Update weight entry */
    suspend fun updateWeight(weightEntry: WeightEntry) = weightDao.update(weightEntry)

    /** Flow of all weight entries, sorted by date */
    fun getAllWeights(): Flow<List<WeightEntry>> = weightDao.getAllWeightsFlow()

    /** Flow of weight points to plot in graph between min and max date */
    fun getWeightPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        weightDao.getWeightPlotData(minDate, maxDate)

    /** Flow of weight entries from the last year */
    fun getRecentWeights(): Flow<List<WeightEntry>> {
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
    fun getAllHba1c(): Flow<List<HBA1CEntry>> = hba1cDao.getAllHBA1CFlow()

    /** Flow of weight points to plot in graph between min and max date */
    fun getHba1cPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        hba1cDao.getHBA1CPlotData(minDate, maxDate)

    /** Flow of HBA1C entries from the last year */
    fun getRecentHba1c(): Flow<List<HBA1CEntry>> {
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
    fun getAllAppointments(): Flow<List<Appointment>> = appointmentDao.getAllAppointmentsFlow()

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
    fun getAllTreatments(): Flow<List<Treatment>> = treatmentDao.getAllTreatmentsFlow()

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
    // Important Dates
    // ----------------
    /** Insert a new important date */
    suspend fun insertImportantDate(importantDate: ImportantDate) =
        importantDateDao.insert(importantDate)

    /** Update diagnosis date */
    suspend fun updateImportantDate(importantDate: ImportantDate) =
        importantDateDao.update(importantDate)

    /** Archive appointment */
    suspend fun setArchivedImportantDate(id: Int, archived: Boolean) =
        importantDateDao.setArchived(id, archived)

    /** Flow of all diagnosis dates */
    fun getAllImportantDates(): Flow<List<ImportantDate>> =
        importantDateDao.getAllImportantDates()

    /** Delete a diagnosis date record by Id**/
    suspend fun deleteImportantDate(id: Int) = importantDateDao.deleteById(id)

    // ----------------
    // Medical Devices
    // ----------------
    /** Insert a new device */
    suspend fun insertDevice(device: MedicalDeviceEntry) = medicalDevicesDao.insert(device)

    /** Update device */
    suspend fun updateDevice(device: MedicalDeviceEntry) = medicalDevicesDao.update(device)

    /** Flow of all devices */
    fun getAllDevices(): Flow<List<MedicalDeviceEntry>> = medicalDevicesDao.getAllMedicalDevices()

    /** Flow of current medical devices */
    fun getAllCurrentConsumableDevices(): Flow<List<MedicalDeviceEntry>> =
        medicalDevicesDao.getAllCurrentConsumableMedicalDevices(today = LocalDate.now())

    /** Flow of all non-consumable devices */
    fun getAllNonConsumableDevices(): Flow<List<MedicalDeviceEntry>> =
        medicalDevicesDao.getAllNonConsumableDevices()

    /** Flow of all consumable devices */
    fun getAllConsumableDevices(): Flow<List<MedicalDeviceEntry>> =
        medicalDevicesDao.getAllConsumableDevices()

    /** Flow of all unreported faulty devices **/
    fun getAllUnreportedFaultyDevices(): Flow<List<MedicalDeviceEntry>> =
        medicalDevicesDao.getAllFaultyUnreportedMedicalDevices()

    /** Flow of all reported faulty devices **/
    fun getAllReportedFaultyDevices(): Flow<List<MedicalDeviceEntry>> =
        medicalDevicesDao.getAllFaultyReportedMedicalDevices()

    /** Flow of all faulty batch numbers **/
    fun getAllFaultyBatchNumbers(): Flow<List<FaultyBatchCount>> =
        medicalDevicesDao.getFaultyBatchNumbersCounts()


    /** Flow of upcoming devices expiration dates */
    fun getUpcomingDevicesExpDates(date: LocalDate): Flow<List<MedicalDeviceEntry>> =
        medicalDevicesDao.getUpcomingExpirationDatesFlow(LocalDate.now(), date)

    // ----------------
    // Medical device
    // ----------------
    /** Find a medical device by its code (GTIN or CIP) */
    suspend fun findMedicalDeviceByCode(code: String): MedicalDeviceInfoEntity? =
        medicalDeviceInfo.findByCode(code)

    /** Delete a device record by Id**/
    suspend fun deleteDevice(id: Int) = medicalDevicesDao.deleteById(id)

    // ----------------
    // Generic / Database Utilities
    // ----------------
    /** Clear the entire database and reset autoincrement IDs */
    fun clearAllDataAndReset() = database.clearAllDataAndReset()
}