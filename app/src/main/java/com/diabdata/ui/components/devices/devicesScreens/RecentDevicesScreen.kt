package com.diabdata.ui.components.devices.devicesScreens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.MedicalDeviceInfoEntity
import com.diabdata.ui.components.DataMatrixScannerDialog
import com.diabdata.ui.components.ScanResult
import com.diabdata.ui.components.ScannableTypes
import com.diabdata.ui.components.addDataPopup.AddDataPopup
import com.diabdata.ui.components.devices.components.AddDeviceFab
import com.diabdata.ui.components.devices.components.CurrentConsumableDevicesList
import com.diabdata.ui.components.devices.components.CurrentNonConsumableDevicesList
import com.diabdata.ui.components.layout.SvgIcon
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun RecentDevicesScreen(
    dataViewModel: DataViewModel
) {
    val availability by dataViewModel.dataAvailability.collectAsState()
    var showAddDevicePopup by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    var prefilledDevice by remember { mutableStateOf<MedicalDeviceEntry?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .padding(20.dp)
            .padding(top = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(35.dp)
    ) {
        if (availability.hasDevices) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(35.dp)
            ) {
                CurrentNonConsumableDevicesList(dataViewModel)
                CurrentConsumableDevicesList(dataViewModel)
                Spacer(modifier = Modifier.height(70.dp))
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(20.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 70.dp)
                    .fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.wrapContentWidth()
                ) {
                    SvgIcon(
                        resId = (R.drawable.no_devices_icon_vector),
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

    }

    AddDeviceFab(onSelect = { showAddDevicePopup = true }, onScanClick = { showScanner = true })

    if (showAddDevicePopup) {
        AddDataPopup(
            type = AddableType.DEVICE,
            dataViewModel = dataViewModel,
            onDismiss = {
                showAddDevicePopup = false
            },
            prefilledMedicalDevice = prefilledDevice,
        )
    }

    if (showScanner) {
        DataMatrixScannerDialog(
            onDismiss = { showScanner = false }, onResult = { result ->
                scope.launch {
                    when (result) {
                        is ScanResult.Device -> {
                            val info = result.data
                            val entity = dataViewModel.getMedicalDeviceByCode(info.gtin)
                            Log.d("EXTRACTED-GTIN", info.gtin)

                            if (entity != null) {
                                val device = generateMedicalDeviceEntry(result, entity)
                                prefilledDevice = device
                                showAddDevicePopup = true
                            } else {
                                Toast.makeText(
                                    context,
                                    "Appareil inconnu pour GTIN ${info.gtin}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        else -> Unit
                    }
                    showScanner = false
                }
            }, visible = showScanner, scannedType = ScannableTypes.DEVICE
        )
    }
}

fun generateMedicalDeviceEntry(
    scanResult: ScanResult.Device, medicalDeviceInfo: MedicalDeviceInfoEntity
): MedicalDeviceEntry {
    val today = LocalDate.now()

    if (scanResult.data.lot == null) {
        throw Exception("Lot number is null")
    }

    val deviceEntry = MedicalDeviceEntry(
        date = today,
        lifeSpanEndDate = today.plusDays(medicalDeviceInfo.daysLifespan.toLong()),
        name = medicalDeviceInfo.fullName,
        batchNumber = scanResult.data.lot,
        serialNumber = scanResult.data.serialNumber,
        manufacturer = medicalDeviceInfo.manufacturer,
        deviceType = medicalDeviceInfo.deviceType,
        createdAt = today,
        isArchived = false,
        lifeSpan = medicalDeviceInfo.daysLifespan,
        isFaulty = false,
        isReported = false,
        isLifeSpanOver = false,
        updatedAt = today,
        referenceNumber = scanResult.data.referenceNumber,
    )

    return deviceEntry
}