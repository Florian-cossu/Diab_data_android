package com.diabdata.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.diabdata.data.DataViewModel
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.ui.components.AddDataFab
import com.diabdata.ui.components.DataMatrixScannerDialog
import com.diabdata.ui.components.ScanResult
import com.diabdata.ui.components.ScannableTypes
import com.diabdata.ui.components.addDataPopup.AddDataPopup
import com.diabdata.ui.components.latestMeasurements.LatestMeasurements
import com.diabdata.ui.components.noDataView.NoDataView
import com.diabdata.utils.MedicationInfo
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

    Scaffold { padding ->
        if (hasData) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                LatestMeasurements(
                    viewModel = dataViewModel
                )
            }
        } else {
            NoDataView()
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
                                    Toast.makeText(
                                        context,
                                        context.getString(
                                            shared.string.toast_data_unknown_medication_code,
                                            info.gtin
                                        ),
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                })
        }


        AddDataFab(
            onSelect = setSelectedType,
            onScanClick = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        )
    }
}

private fun mapToTreatment(info: MedicationInfo, entity: MedicationEntity): Treatment {
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