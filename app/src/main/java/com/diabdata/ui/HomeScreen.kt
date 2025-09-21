package com.diabdata.ui

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.MedicationEntity
import com.diabdata.models.Treatment
import com.diabdata.ui.components.AddDataFab
import com.diabdata.ui.components.DataMatrixScannerDialog
import com.diabdata.ui.components.ScannableTypes
import com.diabdata.ui.components.addDataPopup.AddDataPopup
import com.diabdata.ui.components.latestMeasurements.LatestMeasurements
import com.diabdata.utils.MedicationInfo
import com.diabdata.utils.SvgIcon
import kotlinx.coroutines.launch
import java.time.LocalDate

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
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
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
                            .width((LocalWindowInfo.current.containerSize.width * 0.15f).dp)
                            .aspectRatio(1f),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )

                    Text(
                        text = stringResource(R.string.homescreen_no_data_text),
                        modifier = Modifier.padding(top = 16.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }
        }

        if (showScanner) {
            DataMatrixScannerDialog(
                onDismiss = { showScanner = false },
                onResult = { info ->
                    scope.launch {
                        val entity = dataViewModel.getMedicationByGtin(
                            info.gtin.replace(
                                regex = Regex("^0"), replacement = ""
                            )
                        )
                        Log.d(
                            "EXTRACTED-GTIN",
                            info.gtin.replace(regex = Regex("^0"), replacement = "")
                        )
                        if (entity != null) {
                            val treatment = mapToTreatment(info, entity)
                            Log.d("EXTRACTED-DATA", "$entity")
                            dataViewModel.updatePrefilledTreatment(treatment)
                            setSelectedType(AddableType.TREATMENT)
                        } else {
                            Toast.makeText(
                                context, "Médicament inconnu pour GTIN ${
                                    info.gtin.replace(
                                        regex = Regex("^0"), replacement = ""
                                    )
                                }", Toast.LENGTH_SHORT
                            ).show()
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