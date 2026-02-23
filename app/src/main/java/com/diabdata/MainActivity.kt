package com.diabdata

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DataViewModelFactory
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.theme.DiabDataTheme
import com.diabdata.utils.PermissionManager

class MainActivity : ComponentActivity() {
    private lateinit var dataViewModel: DataViewModel

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DiabDataDatabase.getDatabase(this)
        val repository = DataRepository(
            weightDao = db.weightDao(),
            hba1cDao = db.hba1cDao(),
            appointmentDao = db.appointmentDao(),
            treatmentDao = db.treatmentDao(),
            importantDateDao = db.importantDateDao(),
            medicationDao = db.medicationDao(),
            medicalDevicesDao = db.medicalDevicesDao(),
            medicalDeviceInfo = db.medicalDevicesInfoDao(),
            userDetailsDao = db.userDetailsDao(),
            database = db
        )
        val factory = DataViewModelFactory(repository, applicationContext as Application)
        dataViewModel = ViewModelProvider(this, factory)[DataViewModel::class.java]

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme

            DiabDataTheme {
                RequestPermissionsOnLaunch()
                App(db, dataViewModel)
            }
        }
    }

    @Composable
    fun RequestPermissionsOnLaunch() {
        val permissions = remember { PermissionManager.getRequiredPermissions() }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            results.forEach { (permission, granted) ->
                Log.d("Permissions", "$permission: ${if (granted) "✅" else "❌"}")
            }
        }

        LaunchedEffect(Unit) {
            val context =
                launcher.launch(permissions.toTypedArray())
        }
    }
}