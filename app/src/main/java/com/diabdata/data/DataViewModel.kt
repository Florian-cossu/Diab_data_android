package com.diabdata.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataViewModel(private val repository: DataRepository) : ViewModel() {

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

    // Chargement général
    fun loadAllData() {
        viewModelScope.launch {
            _weights.value = repository.getAllWeights()
            _hba1cEntries.value = repository.getAllHba1c()
            _appointments.value = repository.getAllAppointments()
            _treatments.value = repository.getAllTreatments()
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
}
