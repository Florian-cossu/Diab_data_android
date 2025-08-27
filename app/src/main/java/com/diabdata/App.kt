package com.diabdata

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.diabdata.data.DataViewModel
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.DatabaseEditionView
import com.diabdata.ui.HomeScreen
import com.diabdata.ui.SettingsScreen
import com.diabdata.ui.components.graphsViewer.GraphViewer
import com.diabdata.utils.MedicationInitializer
import com.diabdata.utils.SvgIcon

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun App(
    db: DiabDataDatabase, dataViewModel: DataViewModel
) {
    val context = LocalContext.current.applicationContext
    var selectedTab by rememberSaveable { mutableStateOf("home") }

    LaunchedEffect(Unit) {
        MedicationInitializer(context, db).initialize()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = "Accueil"
                        )
                    },
                    label = { Text(stringResource(R.string.home_menu_title)) },
                    selected = selectedTab == "home",
                    onClick = { selectedTab = "home" })
                NavigationBarItem(
                    icon = {
                        SvgIcon(
                            resId = R.drawable.chart_filled_icon_vector,
                            color = if (selectedTab == "charts") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text(stringResource(R.string.chart_menu_title)) },
                    selected = selectedTab == "charts",
                    onClick = { selectedTab = "charts" })
                NavigationBarItem(
                    icon = {
                        SvgIcon(
                            resId = R.drawable.database_filled_icon_vector,
                            color = if (selectedTab == "data") MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    label = { Text(stringResource(R.string.database_management_menu_title)) },
                    selected = selectedTab == "data",
                    onClick = { selectedTab = "data" })
                NavigationBarItem(
                    icon = {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Paramètres"
                        )
                    },
                    label = { Text(stringResource(R.string.settings_menu_title)) },
                    selected = selectedTab == "settings",
                    onClick = { selectedTab = "settings" })
            }
        }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                "home" -> HomeScreen(dataViewModel = dataViewModel)
                "charts" -> GraphViewer(viewModel = dataViewModel)
                "data" -> DatabaseEditionView(dataViewModel = dataViewModel)
                "settings" -> SettingsScreen(dataViewModel = dataViewModel)
            }
        }
    }
}