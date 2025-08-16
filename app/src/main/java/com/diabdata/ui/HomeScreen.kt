package com.diabdata.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.ui.components.AddDataFab
import com.diabdata.ui.components.AddDataPopup
import com.diabdata.ui.components.latestMeasurements.LatestMeasurements
import com.diabdata.utils.SvgIcon

@Composable
fun HomeScreen(
    weightEntries: List<WeightEntry>,
    hba1cEntries: List<HBA1CEntry>,
    appointments: List<Appointment>,
    treatments: List<Treatment>,
    diagnosisDates: List<DiagnosisDate>,
    dataViewModel: DataViewModel
) {
    val (selectedType, setSelectedType) = remember { mutableStateOf<AddableType?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(
        floatingActionButton = {
            AddDataFab(onSelect = setSelectedType)
        }
    ) { padding ->
        val hasData = listOf(
            weightEntries,
            hba1cEntries,
            appointments,
            treatments,
            diagnosisDates
        ).any { it.isNotEmpty() }

        if (hasData) {
            // Contenu scrollable (les données)
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp, 16.dp, 16.dp, 0.dp)
                    .verticalScroll(scrollState)
            ) {
                LatestMeasurements(
                    weightEntries = weightEntries,
                    hba1cEntries = hba1cEntries,
                    diagnosisEntries = diagnosisDates,
                    appointmentEntries = appointments,
                    treatmentEntries = treatments
                )
            }
        } else {
            // Message "No data" centré verticalement et horizontalement
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    SvgIcon(
                        resId = (R.drawable.inbox_icon_vector),
                        modifier = Modifier
                            .width(LocalConfiguration.current.screenWidthDp.dp * 0.4f)
                            .aspectRatio(1f),
                        color = MaterialTheme.colorScheme.surfaceTint
                    )

                    Text(
                        text = "Aucune données",
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                }
            }
        }

        // Popup pour ajouter une entrée
        selectedType?.let { type ->
            AddDataPopup(
                type = type,
                dataViewModel = dataViewModel,
                onSubmit = { data ->
                    setSelectedType(null)
                },
                onDismiss = {
                    setSelectedType(null)
                }
            )
        }
    }
}