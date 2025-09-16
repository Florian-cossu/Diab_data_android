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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.diabdata.data.DataViewModel
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.HomeScreen
import com.diabdata.ui.components.applicationSettings.SettingsScreen
import com.diabdata.ui.components.databaseView.DatabaseEditionView
import com.diabdata.ui.components.devices.DevicesScreen
import com.diabdata.ui.components.graphsViewer.GraphViewer
import com.diabdata.utils.MedicationInitializer
import com.diabdata.utils.SvgIcon


sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector, val contentDescription: String) : NavIcon
    data class Svg(val resId: Int) : NavIcon
}

data class BottomNavItem(
    val id: String,
    val label: Int,
    val icon: NavIcon,
    val screen: @Composable () -> Unit
)

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun App(
    db: DiabDataDatabase,
    dataViewModel: DataViewModel
) {
    val context = LocalContext.current.applicationContext
    var selectedTab by rememberSaveable { mutableStateOf("home") }

    LaunchedEffect(Unit) {
        MedicationInitializer(context, db).initialize()
    }

    val items = listOf(
        BottomNavItem(
            id = "home",
            label = R.string.home_menu_title,
            icon = NavIcon.Vector(Icons.Filled.Home, "Accueil"),
            screen = { HomeScreen(dataViewModel) }
        ),
        BottomNavItem(
            id = "charts",
            label = R.string.chart_menu_title,
            icon = NavIcon.Svg(R.drawable.chart_filled_icon_vector),
            screen = { GraphViewer(viewModel = dataViewModel) }
        ),
        BottomNavItem(
            id = "data",
            label = R.string.database_management_menu_title,
            icon = NavIcon.Svg(R.drawable.database_filled_icon_vector),
            screen = { DatabaseEditionView(dataViewModel) }
        ),
        BottomNavItem(
            id = "devices",
            label = R.string.devices_menu_title,
            icon = NavIcon.Svg(R.drawable.devices_icon_vector),
            screen = {
                DevicesScreen(dataViewModel)
            }
        ),
        BottomNavItem(
            id = "settings",
            label = R.string.settings_menu_title,
            icon = NavIcon.Vector(Icons.Filled.Settings, "Paramètres"),
            screen = { SettingsScreen(dataViewModel) }
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    val isSelected = item.id == selectedTab
                    NavigationBarItem(
                        icon = {
                            when (val icon = item.icon) {
                                is NavIcon.Vector -> Icon(
                                    icon.imageVector,
                                    icon.contentDescription,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                is NavIcon.Svg -> SvgIcon(
                                    resId = icon.resId,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = { Text(stringResource(item.label)) },
                        selected = isSelected,
                        onClick = { selectedTab = item.id }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items.firstOrNull { it.id == selectedTab }?.screen?.invoke()
        }
    }
}
