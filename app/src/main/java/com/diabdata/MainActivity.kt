package com.diabdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.diabdata.data.DataRepository
import com.diabdata.data.DataViewModel
import com.diabdata.data.DataViewModelFactory
import com.diabdata.data.DiabDataDatabase
import com.diabdata.ui.theme.DiabDataTheme


class MainActivity : ComponentActivity() {
    private lateinit var dataViewModel: DataViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = DiabDataDatabase.getDatabase(this)
        val repository = DataRepository(
            weightDao = db.weightDao(),
            hba1cDao = db.hba1cDao(),
            appointmentDao = db.appointmentDao(),
            treatmentDao = db.treatmentDao(),
            diagnosisDao = db.diagnosisDao(),
            database = db
        )

        val factory = DataViewModelFactory(repository)

        dataViewModel = ViewModelProvider(this, factory).get(DataViewModel::class.java)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkTheme = isSystemInDarkTheme()

            // Update status bar icon colors
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = !darkTheme
            setContent {
                DiabDataTheme {
                    App()
                }
            }
        }
    }
}

