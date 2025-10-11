package com.diabdata

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diabdata.data.DataViewModel
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.HomeScreen
import com.diabdata.ui.components.applicationSettings.SettingsScreen
import com.diabdata.ui.components.databaseView.DatabaseEditionView
import com.diabdata.ui.components.devices.DevicesScreen
import com.diabdata.ui.components.graphsViewer.GraphViewer
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.MedicalDevicesInitializer
import com.diabdata.utils.MedicationInitializer


sealed interface NavIcon {
    data class Vector(val imageVector: ImageVector, val contentDescription: String) : NavIcon
    data class Svg(val resId: Int) : NavIcon
}

data class BottomNavItem(
    val route: String, val label: Int, val selectedIcon: NavIcon, val unselectedIcon: NavIcon
)

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun App(
    db: DiabDataDatabase, dataViewModel: DataViewModel
) {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        MedicationInitializer(context, db).initialize()
        MedicalDevicesInitializer(context, db).initialize()
    }

    val items = listOf(
        BottomNavItem(
            route = "home",
            label = R.string.home_menu_title,
            unselectedIcon = NavIcon.Svg(R.drawable.home_icon_vector),
            selectedIcon = NavIcon.Svg(R.drawable.home_filled_icon_vector)
        ), BottomNavItem(
            route = "charts",
            label = R.string.chart_menu_title,
            unselectedIcon = NavIcon.Svg(R.drawable.chart_icon_vector),
            selectedIcon = NavIcon.Svg(R.drawable.chart_filled_icon_vector)
        ), BottomNavItem(
            route = "data",
            label = R.string.database_management_menu_title,
            unselectedIcon = NavIcon.Svg(R.drawable.database_icon_vector),
            selectedIcon = NavIcon.Svg(R.drawable.database_filled_icon_vector)
        ), BottomNavItem(
            route = "devices",
            label = R.string.devices_menu_title,
            unselectedIcon = NavIcon.Svg(R.drawable.devices_icon_vector),
            selectedIcon = NavIcon.Svg(R.drawable.devices_filled_icon_vector)
        ), BottomNavItem(
            route = "settings",
            label = R.string.settings_menu_title,
            unselectedIcon = NavIcon.Svg(R.drawable.settings_icon_vector),
            selectedIcon = NavIcon.Svg(R.drawable.settings_filled_icon_vector)
        )
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentDestination = currentBackStack?.destination

                items.forEach { item ->
                    val selected = currentDestination?.route == item.route

                    NavigationBarItem(
                        icon = {
                            val icon = if (selected) item.selectedIcon else item.unselectedIcon

                            when (icon) {
                                is NavIcon.Vector -> Icon(
                                    icon.imageVector,
                                    icon.contentDescription,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                is NavIcon.Svg -> SvgIcon(
                                    resId = icon.resId,
                                    color = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        label = { Text(stringResource(item.label)) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        })
                }
            }
        }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300)) + fadeIn(
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300)) + fadeOut(
                    animationSpec = tween(300)
                )
            }) {
            composable("home") { HomeScreen(dataViewModel) }
            composable("charts") { GraphViewer(viewModel = dataViewModel) }
            composable("data") { DatabaseEditionView(dataViewModel) }
            composable("devices") { DevicesScreen(dataViewModel) }
            composable("settings") { SettingsScreen(dataViewModel) }
        }
    }
}