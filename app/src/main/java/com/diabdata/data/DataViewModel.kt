package com.diabdata.data

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.AppointmentType
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.ui.DbEntry
import com.diabdata.utils.AppointmentTypeAdapter
import com.diabdata.utils.LocalDateAdapter
import com.diabdata.utils.MedicationInitializer
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DataViewModel(
    private val repository: DataRepository,
    application: Application
) : AndroidViewModel(application) {
    val appContext: Context = getApplication<Application>().applicationContext

    init {
        viewModelScope.launch(Dispatchers.IO) {
            MedicationInitializer(appContext, repository.database).initialize()
        }
    }

    // Load all data
    val weights: StateFlow<List<WeightEntry>> = repository.getAllWeightsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hba1cEntries: StateFlow<List<HBA1CEntry>> = repository.getAllHba1cFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.getAllAppointmentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val treatments: StateFlow<List<Treatment>> = repository.getAllTreatmentsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val diagnosis: StateFlow<List<DiagnosisDate>> = repository.getAllDiagnosisDatesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Get recent and upcoming data
    val recentHba1c: StateFlow<List<HBA1CEntry>> =
        repository.getRecentHba1cFlow()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Weights
    val recentWeights: StateFlow<List<WeightEntry>> =
        repository.getRecentWeightsFlow()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val upcomingAppointment: StateFlow<List<Appointment>> =
        repository.getUpcomingAppointments()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    fun addDiagnosisDate(diagnosisDate: DiagnosisDate) {
        viewModelScope.launch {
            repository.insertDiagnosisDate(diagnosisDate)
        }
    }

    // Deletion functions
    fun deleteEntry(entry: DbEntry) = viewModelScope.launch {
        when (entry.type) {
            AddableType.WEIGHT -> repository.deleteWeight(entry.id)
            AddableType.HBA1C -> repository.deleteHba1c(entry.id)
            AddableType.APPOINTMENT -> repository.deleteAppointment(entry.id)
            AddableType.TREATMENT -> repository.deleteTreatment(entry.id)
            AddableType.DIAGNOSIS -> repository.deleteDiagnosis(entry.id)
        }
    }

    fun clearDatabase() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.clearAllDataAndReset()
        }
    }

    // Section for scanned medication
    var prefilledTreatment: Treatment? by mutableStateOf(null)
    fun updatePrefilledTreatment(t: Treatment?) {
        prefilledTreatment = t
    }

    suspend fun getMedicationByGtin(gtin: String): MedicationEntity? {
        return repository.findMedicationByCode(gtin)
    }

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
            diagnosisDates = diagnosis.value
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
                repository.insertWeight(weight.copy(id = 0)) // Reset IDs to have them auto incremented by Room to prevent app crashes
            }
            importedData.hba1c.forEach { hba1c ->
                repository.insertHba1c(hba1c.copy(id = 0))
            }
            importedData.appointments.forEach { appointment ->
                repository.insertAppointment(appointment.copy(id = 0))
            }
            importedData.treatments.forEach { treatment ->
                repository.insertTreatment(treatment.copy(id = 0))
            }
            importedData.diagnosisDates.forEach { diagnosis ->
                repository.insertDiagnosisDate(diagnosis.copy(id = 0))
            }
        }
    }
}
