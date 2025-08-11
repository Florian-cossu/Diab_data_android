package com.diabdata

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.LayoutDirection
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DataViewModelFactory
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.HomeScreen
import com.diabdata.ui.SettingsScreen

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

    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                    label = { Text("Accueil") },
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Paramètres") },
                    label = { Text("Paramètres") },
                    selected = selectedTab == "settings",
                    onClick = { selectedTab = "settings" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(
                bottom = paddingValues.calculateBottomPadding(),
                start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                end = paddingValues.calculateEndPadding(LayoutDirection.Ltr)
            )
        ) {
            when (selectedTab) {
                "home" -> HomeScreen(
                    weightEntries = weightEntries.value,
                    hba1cEntries = hba1cEntries.value,
                    appointments = appointments.value,
                    treatments = treatments.value,
                    diagnosisDates = diagnosisDate.value,
                    dataViewModel = dataViewModel
                )
                "settings" -> SettingsScreen()
            }
        }
    }
}