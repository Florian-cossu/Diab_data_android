package com.diabdata

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DataViewModelFactory
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.HomeScreen

@Composable
fun App() {
    val context = LocalContext.current
    val db = DiabDataDatabase.getDatabase(context)
    val repository = DataRepository(
        db.weightDao(),
        db.hba1cDao(),
        db.appointmentDao(),
        db.treatmentDao(),
        db.diagnosisDao()
    )
    val factory = DataViewModelFactory(repository)
    val dataViewModel: DataViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

    val weightEntries = dataViewModel.weights.collectAsState(initial = emptyList())
    val hba1cEntries = dataViewModel.hba1cEntries.collectAsState(initial = emptyList())
    val appointments = dataViewModel.appointments.collectAsState(initial = emptyList())
    val treatments = dataViewModel.treatments.collectAsState(initial = emptyList())
    val diagnosisDate = dataViewModel.diagnosis.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        dataViewModel.loadAllData()
    }

    HomeScreen(
        weightEntries = weightEntries.value,
        hba1cEntries = hba1cEntries.value,
        appointments = appointments.value,
        treatments = treatments.value,
        diagnosisDates = diagnosisDate.value,
        dataViewModel = dataViewModel
    )
}
