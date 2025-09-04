package com.diabdata.data

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.diabdata.R
import com.diabdata.data.converters.toEntity
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.AppointmentType
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.models.TreatmentType
import com.diabdata.models.WeightEntry
import com.diabdata.models.classes.PlotPoint
import com.diabdata.utils.AppointmentTypeAdapter
import com.diabdata.utils.LocalDateAdapter
import com.diabdata.utils.MedicationInitializer
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DataViewModel(
    val repository: DataRepository,
    application: Application
) : AndroidViewModel(application) {
    val appContext: Context = getApplication<Application>().applicationContext

    init {
        viewModelScope.launch(Dispatchers.IO) {
            MedicationInitializer(appContext, repository.database).initialize()
        }
    }

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

    // Helpers to check if we have Data
    data class DataAvailability(
        val hasAnyData: Boolean,
        val hasWeights: Boolean,
        val hasAppointments: Boolean,
        val hasTreatments: Boolean,
        val hasImportantDates: Boolean,
        val hasHba1c: Boolean
    ) {
        companion object {
            val EMPTY = DataAvailability(
                hasAnyData = false,
                hasWeights = false,
                hasAppointments = false,
                hasTreatments = false,
                hasImportantDates = false,
                hasHba1c = false
            )
        }
    }

    val dataAvailability: StateFlow<DataAvailability> = combine(
        weights, hba1cEntries, appointments, treatments, importantDates
    ) { w, h, a, t, d ->
        DataAvailability(
            hasAnyData = w.isNotEmpty() || h.isNotEmpty() || a.isNotEmpty() || t.isNotEmpty() || d.isNotEmpty(),
            hasWeights = w.isNotEmpty(),
            hasAppointments = a.isNotEmpty(),
            hasTreatments = t.isNotEmpty(),
            hasImportantDates = d.isNotEmpty(),
            hasHba1c = h.isNotEmpty()
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
    val upcomingExpirationDates: StateFlow<List<Treatment>> =
        repository.getUpcomingExpDates(LocalDate.now())
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
        }
    }

    // Update function
    suspend fun updateEntry(entry: MixedDbEntry) {
        when (entry.addableType) {
            AddableType.WEIGHT -> repository.updateWeight(entry.toEntity() as WeightEntry)
            AddableType.HBA1C -> repository.updateHBA1C(entry.toEntity() as HBA1CEntry)
            AddableType.APPOINTMENT -> repository.updateAppointment(entry.toEntity() as Appointment)
            AddableType.TREATMENT -> repository.updateTreatment(entry.toEntity() as Treatment)
            AddableType.IMPORTANT_DATE -> repository.updateImportantDate(entry.toEntity() as ImportantDate)
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
    }

    fun getIconForMixedEntry(
        addableType: AddableType,
        appointmentType: AppointmentType? = null,
        treatmentType: TreatmentType? = null
    ): Int {
        return when (addableType) {
            AddableType.WEIGHT -> R.drawable.weight_icon_vector
            AddableType.HBA1C -> R.drawable.hba1c_icon_vector
            AddableType.APPOINTMENT -> when (appointmentType) {
                AppointmentType.ANNUAL_CHECKUP -> R.drawable.recurring_event_icon_vector
                AppointmentType.APPOINTMENT -> R.drawable.stethoscope_icon_vector
                null -> R.drawable.event_icon_vector
            }

            AddableType.TREATMENT -> when (treatmentType) {
                TreatmentType.FAST_ACTING_INSULIN_CARTRIDGE -> R.drawable.fast_acting_insulin_cartridge_icon_vector
                TreatmentType.FAST_ACTING_INSULIN_SYRINGE -> R.drawable.fast_acting_insulin_syringe_icon_vector
                TreatmentType.FAST_ACTING_INSULIN_VIAL -> R.drawable.fast_acting_insulin_vial_icon_vector
                TreatmentType.SLOW_ACTING_INSULIN_CARTRIDGE -> R.drawable.slow_acting_insulin_cartridge_icon_vector
                TreatmentType.SLOW_ACTING_INSULIN_SYRINGE -> R.drawable.slow_acting_insulin_syringe_icon_vector
                TreatmentType.SLOW_ACTING_INSULIN_VIAL -> R.drawable.slow_acting_insulin_vial_icon_vector
                TreatmentType.GLUCAGON_SYRINGE -> R.drawable.syringe_icon_vector
                TreatmentType.GLUCAGON_SPRAY -> R.drawable.nasal_spray_icon_vector
                null -> R.drawable.medication_icon_vector
            }

            AddableType.IMPORTANT_DATE -> R.drawable.important_date_icon_vector
        }
    }


    val allMixedEntries: Flow<List<MixedDbEntry>> = combine(
        appointments,
        importantDates,
        hba1cEntries,
        treatments,
        weights
    ) { appointments, diagnosis, hba1c, treatments, weights ->
        buildList<MixedDbEntry> {
            appointments.forEach {
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

            diagnosis.forEach {
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

            hba1c.forEach {
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

            treatments.forEach {
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

            weights.forEach {
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
            importantDates = importantDates.value
        )

        return gson.toJson(exportData)
    }

    fun importDataFromJsonString(json: String) {
        val gson = GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(AppointmentType::class.java, AppointmentTypeAdapter())
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
        }
    }
}
