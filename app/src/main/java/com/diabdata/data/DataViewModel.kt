package com.diabdata.data

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.diabdata.data.converters.toEntity
import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.MedicalDeviceInfoEntity
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.models.UserDetails
import com.diabdata.models.WeightEntry
import com.diabdata.models.classes.PlotPoint
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.shared.utils.dataTypes.AppointmentType
import com.diabdata.shared.utils.dataTypes.BloodType
import com.diabdata.shared.utils.dataTypes.DiabetesType
import com.diabdata.shared.utils.dataTypes.Gender
import com.diabdata.shared.utils.dataTypes.GlucoseUnit
import com.diabdata.shared.utils.dataTypes.MedicalDeviceInfoType
import com.diabdata.shared.utils.dataTypes.TreatmentType
import com.diabdata.utils.AppointmentTypeAdapter
import com.diabdata.utils.EnumTypeAdapter
import com.diabdata.utils.LocalDateAdapter
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import com.diabdata.shared.R as shared

class DataViewModel(
    val repository: DataRepository,
    application: Application
) : AndroidViewModel(application) {
    // Load all data
    val weights: StateFlow<List<WeightEntry>> = repository.getAllWeights()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hba1cEntries: StateFlow<List<HBA1CEntry>> = repository.getAllHba1c()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val treatments: StateFlow<List<Treatment>> = repository.getAllTreatments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importantDates: StateFlow<List<ImportantDate>> = repository.getAllImportantDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicalDevices: StateFlow<List<MedicalDeviceEntry>> = repository.getAllDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDetails: StateFlow<UserDetails?> = repository.getUserDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // Helpers to check if we have Data
    data class DataAvailability(
        val hasAnyData: Boolean,
        val hasWeights: Boolean,
        val hasAppointments: Boolean,
        val hasTreatments: Boolean,
        val hasImportantDates: Boolean,
        val hasHba1c: Boolean,
        val hasDevices: Boolean
    ) {
        companion object {
            val EMPTY = DataAvailability(
                hasAnyData = false,
                hasWeights = false,
                hasAppointments = false,
                hasTreatments = false,
                hasImportantDates = false,
                hasHba1c = false,
                hasDevices = false
            )
        }
    }

    val part1 = combine(weights, hba1cEntries, appointments) { w, h, a ->
        Triple(w, h, a)
    }

    val part2 = combine(treatments, importantDates, medicalDevices) { t, d, md ->
        Triple(t, d, md)
    }

    val dataAvailability: StateFlow<DataAvailability> =
        combine(part1, part2) { (w, h, a), (t, d, md) ->
            DataAvailability(
                hasAnyData = w.isNotEmpty() || h.isNotEmpty() || a.isNotEmpty() ||
                        t.isNotEmpty() || d.isNotEmpty() || md.isNotEmpty(),
                hasWeights = w.isNotEmpty(),
                hasAppointments = a.isNotEmpty(),
                hasTreatments = t.isNotEmpty(),
                hasImportantDates = d.isNotEmpty(),
                hasHba1c = h.isNotEmpty(),
                hasDevices = md.isNotEmpty()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataAvailability.EMPTY)

    // Get recent and upcoming data or plot data
    // HBA1C
    val recentHba1c: StateFlow<List<HBA1CEntry>> =
        repository.getRecentHba1c()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getHba1cPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        repository.getHba1cPlotData(minDate, maxDate)

    // Weights
    val recentWeights: StateFlow<List<WeightEntry>> =
        repository.getRecentWeights()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getWeightPlotData(minDate: LocalDate, maxDate: LocalDate): Flow<List<PlotPoint>> =
        repository.getWeightPlotData(minDate, maxDate)

    // Appointments
    val upcomingAppointment: StateFlow<List<Appointment>> =
        repository.getUpcomingAppointments()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Expiration dates
    val upcomingExpiringTreatmentDates: StateFlow<List<Treatment>> =
        repository.getUpcomingExpDates(LocalDate.now())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current devices
    val currentConsumableDevices: StateFlow<List<MedicalDeviceEntry>> =
        repository.getAllCurrentConsumableDevices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nonConsumableDevices: StateFlow<List<MedicalDeviceEntry>> =
        repository.getAllNonConsumableDevices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val consumableDevices: StateFlow<List<MedicalDeviceEntry>> =
        repository.getAllConsumableDevices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faultyDevices: StateFlow<List<MedicalDeviceEntry>> =
        repository.getAllUnreportedFaultyDevices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportedFaultyDevices: StateFlow<List<MedicalDeviceEntry>> =
        repository.getAllReportedFaultyDevices()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val faultyBatchNumbersTableData: StateFlow<List<List<String>>> =
        repository.getAllFaultyBatchNumbers()
            .map { list ->
                list.map { entry ->
                    listOf(entry.batchNumber, entry.count.toString())
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // Insertion functions
    fun addWeight(weightEntry: WeightEntry) {
        viewModelScope.launch {
            repository.insertWeight(weightEntry)
        }
    }

    fun addHba1c(hba1cEntry: HBA1CEntry) {
        viewModelScope.launch {
            repository.insertHba1c(hba1cEntry)
        }
    }

    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.insertAppointment(appointment)
        }
    }

    fun addTreatment(treatment: Treatment) {
        viewModelScope.launch {
            repository.insertTreatment(treatment)
        }
    }

    fun addImportantDate(importantDate: ImportantDate) {
        viewModelScope.launch {
            repository.insertImportantDate(importantDate)
        }
    }

    fun insertDevice(device: MedicalDeviceEntry) {
        viewModelScope.launch {
            repository.insertDevice(device)
        }
    }

    // Archive function
    fun setArchived(entry: MixedDbEntry, archived: Boolean) {
        viewModelScope.launch {
            when (entry.addableType) {
                AddableType.WEIGHT -> repository.updateWeight(
                    (entry.toEntity() as WeightEntry).copy(
                        isArchived = archived,
                        updatedAt = LocalDate.now()
                    )

                )

                AddableType.HBA1C -> repository.updateHBA1C(
                    (entry.toEntity() as HBA1CEntry).copy(
                        isArchived = archived,
                        updatedAt = LocalDate.now()
                    )

                )

                AddableType.APPOINTMENT -> repository.updateAppointment(
                    (entry.toEntity() as Appointment).copy(
                        isArchived = archived,
                        updatedAt = LocalDate.now()
                    )

                )

                AddableType.TREATMENT -> repository.updateTreatment(
                    (entry.toEntity() as Treatment).copy(
                        isArchived = archived,
                        updatedAt = LocalDate.now()
                    )

                )

                AddableType.IMPORTANT_DATE -> repository.updateImportantDate(
                    (entry.toEntity() as ImportantDate).copy(
                        isArchived = archived,
                        updatedAt = LocalDate.now()
                    )

                )

                AddableType.DEVICE -> repository.updateDevice(
                    (entry.toEntity() as MedicalDeviceEntry).copy(
                        isArchived = archived,
                        updatedAt = LocalDate.now()
                    )
                )
            }
        }
    }

    // Deletion functions
    fun deleteEntry(entry: MixedDbEntry) = viewModelScope.launch {
        when (entry.addableType) {
            AddableType.WEIGHT -> repository.deleteWeight(entry.id)
            AddableType.HBA1C -> repository.deleteHba1c(entry.id)
            AddableType.APPOINTMENT -> repository.deleteAppointment(entry.id)
            AddableType.TREATMENT -> repository.deleteTreatment(entry.id)
            AddableType.IMPORTANT_DATE -> repository.deleteImportantDate(entry.id)
            AddableType.DEVICE -> repository.deleteDevice(entry.id)
        }
    }

    // Delete user details
    fun deleteUserDetails() = viewModelScope.launch {
        repository.deleteUserDetails()
    }

    // Update function
    suspend fun updateEntry(entry: MixedDbEntry) {
        when (entry.addableType) {
            AddableType.WEIGHT -> repository.updateWeight(entry.toEntity() as WeightEntry)
            AddableType.HBA1C -> repository.updateHBA1C(entry.toEntity() as HBA1CEntry)
            AddableType.APPOINTMENT -> repository.updateAppointment(entry.toEntity() as Appointment)
            AddableType.TREATMENT -> repository.updateTreatment(entry.toEntity() as Treatment)
            AddableType.IMPORTANT_DATE -> repository.updateImportantDate(entry.toEntity() as ImportantDate)
            AddableType.DEVICE -> repository.updateDevice(entry.toEntity() as MedicalDeviceEntry)
        }
    }

    suspend fun updateDevice(device: MedicalDeviceEntry) = repository.updateDevice(device)

    // Update user details
    fun updateUserDetails(userDetails: UserDetails) {
        viewModelScope.launch {
            repository.updateUserDetails(userDetails)
        }
    }

    // DataViewModel
    fun saveProfilePhoto(uri: Uri, onSaved: (String) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val fileName = "profile_photo_${System.currentTimeMillis()}.jpg"
            val file = File(application.filesDir, fileName)

            application.filesDir.listFiles()
                ?.filter { it.name.startsWith("profile_photo_") && it.name != fileName }
                ?.forEach { it.delete() }

            application.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }

            val path = file.absolutePath
            repository.addProfilePhotoPath(path)

            withContext(Dispatchers.Main) {
                onSaved(path)
            }
        }
    }

    suspend fun updateProfilePhotoPath(path: String) {
        repository.addProfilePhotoPath(path)
    }

    fun getAllFaultyDevicesExpiredToday() = repository.getAllFaultyDevicesExpiredToday()

    suspend fun setDevicesLifespanOver(devices: List<MedicalDeviceEntry>, isOver: Boolean = true) {
        for (device in devices) {
            repository.updateDevice(device.copy(isLifeSpanOver = isOver))
        }
    }

    fun clearDatabase(context: Context) = viewModelScope.launch {
        val workManager = WorkManager.getInstance(context)
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        withContext(Dispatchers.IO) {
            repository.clearAllDataAndReset()

            // cancel all reminder workers
            workManager.cancelAllWorkByTag("treatments")
            workManager.cancelAllWorkByTag("appointments")

            // Reset user's reminders preferences
            prefs.edit {
                putBoolean("appointment_reminder", false)
                putBoolean("expiration_reminder", false)
            }
        }
    }

    // Section for collecting full DB
    sealed class MixedDbEntry {
        abstract val id: Int
        abstract val addableType: AddableType
        abstract val date: LocalDate

        abstract val icon: Int
        abstract val isArchived: Boolean
        abstract val createdAt: LocalDate

        abstract val updatedAt: LocalDate

        data class AppointmentEntry(
            override val id: Int,
            override val date: LocalDate,
            override val addableType: AddableType = AddableType.APPOINTMENT,
            val doctor: String,
            val type: AppointmentType,
            val notes: String?,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: LocalDate,
            override val updatedAt: LocalDate
        ) : MixedDbEntry()

        data class ImportantDateEntry(
            override val id: Int,
            override val date: LocalDate,
            override val addableType: AddableType = AddableType.IMPORTANT_DATE,
            val importantDate: String,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: LocalDate,
            override val updatedAt: LocalDate
        ) : MixedDbEntry()

        data class Hba1cEntry(
            override val id: Int,
            override val date: LocalDate,
            override val addableType: AddableType = AddableType.HBA1C,
            val value: Float,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: LocalDate,
            override val updatedAt: LocalDate
        ) : MixedDbEntry()

        data class TreatmentEntry(
            override val id: Int,
            override val date: LocalDate,
            override val addableType: AddableType = AddableType.TREATMENT,
            val name: String,
            val treatmentType: TreatmentType,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: LocalDate,
            override val updatedAt: LocalDate
        ) : MixedDbEntry()

        data class WeightEntry(
            override val id: Int,
            override val date: LocalDate,
            override val addableType: AddableType = AddableType.WEIGHT,
            val value: Float,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: LocalDate,
            override val updatedAt: LocalDate
        ) : MixedDbEntry()

        data class DeviceEntry(
            override val id: Int,
            override val date: LocalDate,
            val lifeSpanEndDate: LocalDate,
            override val addableType: AddableType = AddableType.DEVICE,
            val name: String,
            val deviceType: MedicalDeviceInfoType,
            val batchNumber: String,
            val serialNumber: String,
            val referenceNumber: String,
            val manufacturer: String,
            val lifeSpan: Int,
            val isFaulty: Boolean,
            val isReported: Boolean,
            val isLifeSpanOver: Boolean,
            override val icon: Int,
            override val isArchived: Boolean,
            override val createdAt: LocalDate,
            override val updatedAt: LocalDate
        ) : MixedDbEntry()
    }

    fun getIconForMixedEntry(
        addableType: AddableType,
        appointmentType: AppointmentType? = null,
        treatmentType: TreatmentType? = null,
        deviceType: MedicalDeviceInfoType? = null
    ): Int {
        return when (addableType) {
            AddableType.WEIGHT -> AddableType.WEIGHT.iconRes
            AddableType.HBA1C -> AddableType.HBA1C.iconRes
            AddableType.APPOINTMENT -> when (appointmentType) {
                AppointmentType.ANNUAL_CHECKUP -> AppointmentType.ANNUAL_CHECKUP.iconRes
                AppointmentType.APPOINTMENT -> AppointmentType.APPOINTMENT.iconRes
                null -> shared.drawable.event_icon_vector
            }

            AddableType.TREATMENT -> when (treatmentType) {
                TreatmentType.FAST_ACTING_INSULIN_CARTRIDGE -> TreatmentType.FAST_ACTING_INSULIN_CARTRIDGE.iconRes
                TreatmentType.FAST_ACTING_INSULIN_SYRINGE -> TreatmentType.FAST_ACTING_INSULIN_SYRINGE.iconRes
                TreatmentType.FAST_ACTING_INSULIN_VIAL -> TreatmentType.FAST_ACTING_INSULIN_VIAL.iconRes
                TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE -> TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE.iconRes
                TreatmentType.SLOW_ACTING_INSULIN_SYRINGE -> TreatmentType.SLOW_ACTING_INSULIN_SYRINGE.iconRes
                TreatmentType.SLOW_ACTING_INSULIN_VIAL -> TreatmentType.SLOW_ACTING_INSULIN_VIAL.iconRes
                TreatmentType.B_KETONE_TEST_STRIP -> TreatmentType.B_KETONE_TEST_STRIP.iconRes
                TreatmentType.BLOOD_GLUCOSE_TEST_STRIP -> TreatmentType.BLOOD_GLUCOSE_TEST_STRIP.iconRes
                TreatmentType.GLUCAGON_SYRINGE -> TreatmentType.GLUCAGON_SYRINGE.iconRes
                TreatmentType.GLUCAGON_SPRAY -> TreatmentType.GLUCAGON_SPRAY.iconRes
                TreatmentType.UNKNOWN -> TreatmentType.UNKNOWN.iconRes
                null -> shared.drawable.medication_icon_vector
            }

            AddableType.IMPORTANT_DATE -> shared.drawable.important_date_icon_vector

            AddableType.DEVICE -> when (deviceType) {
                MedicalDeviceInfoType.WIRELESS_PATCH -> MedicalDeviceInfoType.WIRELESS_PATCH.iconRes
                MedicalDeviceInfoType.WIRED_PATCH -> MedicalDeviceInfoType.WIRED_PATCH.iconRes
                MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR -> MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR.iconRes
                MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER -> MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER.iconRes
                MedicalDeviceInfoType.WIRED_PUMP -> MedicalDeviceInfoType.WIRED_PUMP.iconRes
                MedicalDeviceInfoType.WIRELESS_PATCH_REMOTE -> MedicalDeviceInfoType.WIRELESS_PATCH_REMOTE.iconRes
                else -> shared.drawable.devices_icon_vector
            }
        }
    }

    val allMixedEntries: Flow<List<MixedDbEntry>> = combine(
        part1,
        part2
    ) { (w, h, a), (t, d, md) ->
        buildList<MixedDbEntry> {
            a.forEach {
                add(
                    MixedDbEntry.AppointmentEntry(
                        id = it.id,
                        date = it.date,
                        doctor = it.doctor,
                        type = it.type,
                        notes = it.notes,
                        icon = getIconForMixedEntry(
                            AddableType.APPOINTMENT,
                            appointmentType = it.type
                        ),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                )
            }

            d.forEach {
                add(
                    MixedDbEntry.ImportantDateEntry(
                        id = it.id,
                        date = it.date,
                        importantDate = it.importantDate,
                        icon = getIconForMixedEntry(AddableType.IMPORTANT_DATE),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                )
            }

            h.forEach {
                add(
                    MixedDbEntry.Hba1cEntry(
                        id = it.id,
                        date = it.date,
                        value = it.value,
                        icon = getIconForMixedEntry(AddableType.HBA1C),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                )
            }

            t.forEach {
                add(
                    MixedDbEntry.TreatmentEntry(
                        id = it.id,
                        date = it.expirationDate,
                        name = it.name,
                        treatmentType = it.type,
                        icon = getIconForMixedEntry(AddableType.TREATMENT, treatmentType = it.type),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                )
            }

            w.forEach {
                add(
                    MixedDbEntry.WeightEntry(
                        id = it.id,
                        date = it.date,
                        value = it.value,
                        icon = getIconForMixedEntry(AddableType.WEIGHT),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                )
            }

            md.forEach {
                add(
                    MixedDbEntry.DeviceEntry(
                        id = it.id,
                        date = it.date,
                        lifeSpanEndDate = it.lifeSpanEndDate,
                        addableType = AddableType.DEVICE,
                        name = it.name,
                        deviceType = it.deviceType,
                        batchNumber = it.batchNumber,
                        serialNumber = it.serialNumber ?: "",
                        referenceNumber = it.referenceNumber ?: "",
                        manufacturer = it.manufacturer ?: "",
                        lifeSpan = it.lifeSpan,
                        isFaulty = it.isFaulty,
                        isReported = it.isReported,
                        isLifeSpanOver = it.isLifeSpanOver,
                        icon = getIconForMixedEntry(AddableType.DEVICE, deviceType = it.deviceType),
                        isArchived = it.isArchived,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                )
            }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Section for scanned medication
    var prefilledTreatment: Treatment? by mutableStateOf(null)
    fun updatePrefilledTreatment(t: Treatment?) {
        prefilledTreatment = t
    }

    suspend fun getMedicationByGtin(gtin: String): MedicationEntity? {
        return repository.findMedicationByCode(gtin)
    }

    // Section for scanned medical device
    var prefilledMedicalDevice: MedicalDeviceEntry? by mutableStateOf(null)

    fun updatePrefilledMedicalDevice(m: MedicalDeviceEntry?) {
        prefilledMedicalDevice = m
    }

    suspend fun getMedicalDeviceByCode(code: String): MedicalDeviceInfoEntity? {
        return repository.findMedicalDeviceByCode(code)
    }

    // Section for data import/export
    fun exportDataAsJsonString(): String {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .setPrettyPrinting()
            .create()

        val exportData = ExportData(
            weights = weights.value,
            hba1c = hba1cEntries.value,
            appointments = appointments.value,
            treatments = treatments.value,
            importantDates = importantDates.value,
            devices = medicalDevices.value,
            userDetails = userDetails.value
        )

        return gson.toJson(exportData)
    }

    suspend fun importDataFromJsonString(json: String) {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(AppointmentType::class.java, AppointmentTypeAdapter())
            .registerTypeAdapter(DiabetesType::class.java,
                EnumTypeAdapter(DiabetesType::class.java)
            )
            .registerTypeAdapter(Gender::class.java, EnumTypeAdapter(Gender::class.java))
            .registerTypeAdapter(BloodType::class.java, EnumTypeAdapter(BloodType::class.java))
            .registerTypeAdapter(GlucoseUnit::class.java, EnumTypeAdapter(GlucoseUnit::class.java))
            .create()

        val importedData: ExportData = gson.fromJson(json, ExportData::class.java)

        viewModelScope.launch {
            importedData.weights.forEach { weight ->
                repository.insertWeight(weight.copy()) // Reset IDs to have them auto incremented by Room to prevent app crashes
            }
            importedData.hba1c.forEach { hba1c ->
                repository.insertHba1c(hba1c.copy())
            }
            importedData.appointments.forEach { appointment ->
                repository.insertAppointment(appointment.copy())
            }
            importedData.treatments.forEach { treatment ->
                repository.insertTreatment(treatment.copy())
            }
            importedData.importantDates.forEach { diagnosis ->
                repository.insertImportantDate(diagnosis.copy())
            }
            importedData.devices.forEach { device ->
                repository.insertDevice(device.copy())
            }
            importedData.userDetails?.let { userDetails ->
                repository.updateUserDetails(userDetails.copy(profilePhotoPath = null))
            }
        }
    }
}
