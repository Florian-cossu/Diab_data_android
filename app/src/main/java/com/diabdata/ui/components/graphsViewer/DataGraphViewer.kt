package com.diabdata.ui.components.graphsViewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.ui.components.date_components.DateRangeModal
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.formatLocalDate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GraphViewer(
    modifier: Modifier = Modifier,
    viewModel: DataViewModel = viewModel(),
) {
    val scrollState = rememberScrollState()
    val options = listOf(
        stringResource(R.string.time_range_month),
        stringResource(R.string.time_range_year),
        stringResource(R.string.time_range_custom)
    )

    var selectedIndex by remember { mutableIntStateOf(2) }

    var showDateRangePicker by remember { mutableStateOf(false) }
    var customDateRange by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }

    val maxDate = LocalDate.now()

    val minDate = when (selectedIndex) {
        0 -> maxDate.minusMonths(1)
        1 -> maxDate.minusYears(1)
        2 -> customDateRange?.first ?: maxDate.minusYears(2)
        else -> maxDate.minusYears(5)
    }

    val maxDateAdjusted = when (selectedIndex) {
        2 -> customDateRange?.second ?: maxDate
        else -> maxDate
    }

    val weightPoints by viewModel
        .getWeightPlotData(minDate, maxDateAdjusted)
        .collectAsState(initial = emptyList())

    val hba1cPoints by viewModel
        .getHba1cPlotData(minDate, maxDateAdjusted)
        .collectAsState(initial = emptyList())

    var showRegressionLine by remember { mutableStateOf(false) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp
                )
                .padding(20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
            ) {
                options.forEachIndexed { index, label ->
                    ToggleButton(
                        checked = selectedIndex == index,
                        onCheckedChange = { selectedIndex = index },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(0.8f)
                            .height(40.dp)
                            .semantics { role = Role.RadioButton },
                        shapes =
                            when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                    ) {
                        if (selectedIndex == index) {
                            Icon(
                                Icons.Outlined.Check,
                                contentDescription = null,
                                Modifier.size(16.dp)
                            )
                        } else null
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text(label)
                    }
                }
                if (selectedIndex == 2) {
                    IconButton(
                        onClick = { showDateRangePicker = true },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .aspectRatio(1f)
                    ) {
                        SvgIcon(
                            resId = R.drawable.date_range_icon_vector,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                IconButton(
                    onClick = {
                        showRegressionLine = !showRegressionLine
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor =
                            if (showRegressionLine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(40.dp)
                        .aspectRatio(1f)
                ) {
                    SvgIcon(
                        resId = R.drawable.trending_up_icon_vector,
                        color = if (showRegressionLine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (selectedIndex == 2) {
                Row {
                    Text(
                        stringResource(
                            R.string.database_management_time_range_indicator,
                            formatLocalDate(minDate),
                            formatLocalDate(maxDateAdjusted)
                        )
                    )
                }
            }

            LineGraph(
                points = weightPoints,
                label = stringResource(AddableType.WEIGHT.displayNameRes),
                primaryColor = AddableType.WEIGHT.baseColor.toArgb(),
                showTrendLine = showRegressionLine
            )
            LineGraph(
                points = hba1cPoints,
                label = stringResource(AddableType.HBA1C.displayNameRes),
                primaryColor = AddableType.HBA1C.baseColor.toArgb(),
                showTrendLine = showRegressionLine
            )
        }
    }

    if (showDateRangePicker) {
        DateRangeModal(
            initialStartDate = minDate,
            initialEndDate = maxDateAdjusted,
            onDismiss = { showDateRangePicker = false },
            onDateRangeSelected = { start, end ->
                customDateRange = start to end
                showDateRangePicker = false
            }
        )
    }
}