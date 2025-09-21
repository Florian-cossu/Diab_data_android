package com.diabdata.ui.components.devices

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.ui.components.addDataPopup.AddDataPopup
import com.diabdata.ui.components.devices.components.AddDeviceFab
import com.diabdata.utils.SvgIcon

@Composable
fun DevicesScreen(
    dataViewModel: DataViewModel
) {
    val availability by dataViewModel.dataAvailability.collectAsState()
    var showAddDevicePopup by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()


    Scaffold { padding ->
        if (availability.hasDevices) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text("HAS DEVICES")
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

        AddDeviceFab(
            onSelect = { showAddDevicePopup = true },
            onScanClick = { showScanner = true }
        )

        if (showAddDevicePopup) {
            AddDataPopup(
                type = AddableType.DEVICE,
                dataViewModel = dataViewModel,
                onDismiss = {
                    showAddDevicePopup = false
                },
                prefilledMedicalDevice = null,
            )
        }
    }
}