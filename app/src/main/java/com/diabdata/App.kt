package com.diabdata

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DataViewModelFactory
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.DatabaseEditionView
import com.diabdata.ui.HomeScreen
import com.diabdata.ui.SettingsScreen
import com.diabdata.utils.MedicationInitializer
import com.diabdata.utils.RequestNotificationPermission
import com.diabdata.utils.SvgIcon

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun App() {
    val context = LocalContext.current.applicationContext

    RequestNotificationPermission(
        context = context,
        onPermissionGranted = { },
        onPermissionDenied = { }
    )

    val db = DiabDataDatabase.getDatabase(context)
    val repository = DataRepository(
        db.weightDao(),
        db.hba1cDao(),
        db.appointmentDao(),
        db.treatmentDao(),
        db.diagnosisDao(),
        db
    )
    val factory = DataViewModelFactory(repository, context as Application)
    val dataViewModel: DataViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

    val weightEntries = dataViewModel.weights.collectAsState(initial = emptyList())
    val hba1cEntries = dataViewModel.hba1cEntries.collectAsState(initial = emptyList())
    val appointments = dataViewModel.appointments.collectAsState(initial = emptyList())
    val treatments = dataViewModel.treatments.collectAsState(initial = emptyList())
    val diagnosisDate = dataViewModel.diagnosis.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        MedicationInitializer(context, db).initialize()
    }

    var selectedTab by rememberSaveable { mutableStateOf("home") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Accueil") },
                    label = { Text(stringResource(R.string.home_menu_title)) },
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
                    icon = {
                        SvgIcon(
                            resId = R.drawable.chart_filled_icon_vector,
                            color = if (selectedTab == "charts") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    label = { Text(stringResource(R.string.chart_menu_title)) },
                    selected = selectedTab == "charts",
                    onClick = { selectedTab = "charts" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                NavigationBarItem(
                    icon = {
                        SvgIcon(
                            resId = R.drawable.database_filled_icon_vector,
                            color = if (selectedTab == "data") {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    },
                    label = { Text(stringResource(R.string.database_management_menu_title)) },
                    selected = selectedTab == "data",
                    onClick = { selectedTab = "data" },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Paramètres") },
                    label = { Text(stringResource(R.string.settings_menu_title)) },
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
                "chart" -> {}
                "data" -> DatabaseEditionView(
                    dataViewModel = dataViewModel
                )
                "settings" -> SettingsScreen(
                    dataViewModel = dataViewModel
                )
            }
        }
    }
}