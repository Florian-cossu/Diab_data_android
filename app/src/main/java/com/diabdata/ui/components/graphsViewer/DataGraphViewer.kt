package com.diabdata.ui.components.graphsViewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import java.time.LocalDate

@Composable
fun GraphViewer(
    modifier: Modifier = Modifier,
    viewModel: DataViewModel = viewModel(),
) {
    val minDate = remember { LocalDate.now().minusYears(5) }
    val maxDate = remember { LocalDate.now() }

    val weightPoints by viewModel
        .getWeightPlotData(minDate, maxDate)
        .collectAsState(initial = emptyList())

    val hba1cPoints by viewModel
        .getHba1cPlotData(minDate, maxDate)
        .collectAsState(initial = emptyList())

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LineGraph(
                points = weightPoints,
                label = stringResource(AddableType.WEIGHT.displayNameRes)
            )
            LineGraph(
                points = hba1cPoints,
                label = stringResource(AddableType.HBA1C.displayNameRes),
                primaryColor = MaterialTheme.colorScheme.tertiaryFixed.toArgb(),
                showTrendLine = true
            )
        }
    }
}