package com.diabdata.feature.home

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.core.database.DataViewModel
import com.diabdata.core.model.Medication
import com.diabdata.core.model.Treatment
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.core.ui.components.AddDataFab
import com.diabdata.feature.dataMarixScanner.ui.DataMatrixScannerDialog
import com.diabdata.feature.dataMarixScanner.ui.ScanResult
import com.diabdata.feature.dataMarixScanner.ui.ScannableTypes
import com.diabdata.core.ui.components.addDataPopup.AddDataPopup
import com.diabdata.feature.home.components.LatestMeasurements
import com.diabdata.feature.noDataView.NoDataView
import com.diabdata.feature.dataMarixScanner.utils.MedicationInfo
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.diabdata.shared.R as shared

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun HomeScreen(
    dataViewModel: DataViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val availability by dataViewModel.dataAvailability.collectAsState()
    val hasData = availability.hasAnyData
    val (selectedType, setSelectedType) = remember { mutableStateOf<AddableType?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    val unknownGtin = stringResource(shared.string.toast_data_unknown_medication_code)
    val scrollState = rememberScrollState()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted ->
            if (isGranted) {
                showScanner = true
            } else {
                Toast.makeText(
                    context,
                    shared.string.toast_camera_permission_error,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasData) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                LatestMeasurements(viewModel = dataViewModel)
            }
        } else {
            NoDataView()
        }

        AddDataFab(
            onSelect = setSelectedType,
            onScanClick = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }

    if (showScanner) {
        DataMatrixScannerDialog(
            onDismiss = { showScanner = false },
            onResult = { result ->
                scope.launch {
                    when (result) {
                        is ScanResult.Medication -> {
                            val info = result.data
                            val entity = dataViewModel.getMedicationByGtin(
                                info.gtin.replace(regex = Regex("^0"), replacement = "")
                            )
                            if (entity != null) {
                                val treatment = mapToTreatment(info, entity)
                                dataViewModel.updatePrefilledTreatment(treatment)
                                setSelectedType(AddableType.TREATMENT)
                            } else {
                                Toast.makeText(context, unknownGtin, Toast.LENGTH_SHORT).show()
                            }
                        }

                        else -> null
                    }
                    showScanner = false
                }
            },
            visible = showScanner,
            scannedType = ScannableTypes.MEDICATION
        )
    }

    selectedType?.let { type ->
        AddDataPopup(
            type = type,
            dataViewModel = dataViewModel,
            prefilledTreatment = dataViewModel.prefilledTreatment,
            onDismiss = {
                setSelectedType(null)
                dataViewModel.prefilledTreatment = null
            }
        )
    }
}

private fun mapToTreatment(info: MedicationInfo, entity: Medication): Treatment {
    val today = LocalDate.now()
    val expiration =
        info.expiration?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now()

    return Treatment(
        expirationDate = expiration,
        name = entity.fullName,
        createdAt = today,
        isArchived = false,
        type = entity.treatmentType,
        updatedAt = LocalDate.now()
    )
}