package com.diabdata.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diabdata.models.Appointment
import com.diabdata.models.AppointmentType
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.utils.AppointmentTypeAdapter
import com.diabdata.utils.LocalDateAdapter
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DataViewModel(private val repository: DataRepository) : ViewModel() {
    // Flow based queries
    // HBA1C
    val recentHba1c: StateFlow<List<HBA1CEntry>> =
        repository.getRecentHba1cFlow()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Weights
    val recentWeights: StateFlow<List<WeightEntry>> =
        repository.getRecentWeightsFlow()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val upcomingAppointment: StateFlow<List<Appointment>> =
        repository.getUpcomingAoppointments()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Legacy
    // Poids
    private val _weights = MutableStateFlow<List<WeightEntry>>(emptyList())
    val weights: StateFlow<List<WeightEntry>> = _weights

    // HBA1C
    private val _hba1cEntries = MutableStateFlow<List<HBA1CEntry>>(emptyList())
    val hba1cEntries: StateFlow<List<HBA1CEntry>> = _hba1cEntries

    // RDV
    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments

    // Traitements
    private val _treatments = MutableStateFlow<List<Treatment>>(emptyList())
    val treatments: StateFlow<List<Treatment>> = _treatments

    // Diagnosis dates
    private val _diagnosis = MutableStateFlow<List<DiagnosisDate>>(emptyList())
    val diagnosis: StateFlow<List<DiagnosisDate>> = _diagnosis

    // Chargement général
    fun loadAllData() {
        viewModelScope.launch {
            _weights.value = repository.getAllWeights()
            _hba1cEntries.value = repository.getAllHba1c()
            _appointments.value = repository.getAllAppointments()
            _treatments.value = repository.getAllTreatments()
            _diagnosis.value = repository.getAllDiagnosisDate()
        }
    }

    // Ajout d’une entrée poids
    fun addWeight(weightEntry: WeightEntry) {
        viewModelScope.launch {
            repository.insertWeight(weightEntry)
            _weights.value = repository.getAllWeights()
        }
    }

    // Ajout d’une entrée HBA1C
    fun addHba1c(hba1cEntry: HBA1CEntry) {
        viewModelScope.launch {
            repository.insertHba1c(hba1cEntry)
            _hba1cEntries.value = repository.getAllHba1c()
        }
    }

    // Ajout d’un RDV
    fun addAppointment(appointment: Appointment) {
        viewModelScope.launch {
            repository.insertAppointment(appointment)
            _appointments.value = repository.getAllAppointments()
        }
    }

    // Ajout d’un traitement
    fun addTreatment(treatment: Treatment) {
        viewModelScope.launch {
            repository.insertTreatment(treatment)
            _treatments.value = repository.getAllTreatments()
        }
    }

    fun addDiagnosisDate(diagnosisDate: DiagnosisDate) {
        viewModelScope.launch {
            repository.insertDiagnosisDate(diagnosisDate)
            _diagnosis.value = repository.getAllDiagnosisDate()
        }
    }

    fun deleteEntry(id: Int, tableName: String) {
        val rowsDeleted: Int = repository.deleteEntry(id, tableName) // rowsDeleted is Int now
        if (rowsDeleted > 0) {
            when (tableName) {
                "weight_entries" -> _weights.value = _weights.value.filter { it.id != id }
                "hba1c_entries" -> _hba1cEntries.value = _hba1cEntries.value.filter { it.id != id }
                "appointments" -> _appointments.value = _appointments.value.filter { it.id != id }
                "treatments" -> _treatments.value = _treatments.value.filter { it.id != id }
                "diagnosis_date_entries" -> _diagnosis.value =
                    _diagnosis.value.filter { it.id != id }
            }
        }
    }


    fun clearDatabase() = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            repository.clearAllDataAndReset()
        }
        loadAllData()
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
            importedData.weights.forEach { repository.insertWeight(it) }
            importedData.hba1c.forEach { repository.insertHba1c(it) }
            importedData.appointments.forEach { repository.insertAppointment(it) }
            importedData.treatments.forEach { repository.insertTreatment(it) }
            importedData.diagnosisDates.forEach { repository.insertDiagnosisDate(it) }

            loadAllData()
        }
    }
}
