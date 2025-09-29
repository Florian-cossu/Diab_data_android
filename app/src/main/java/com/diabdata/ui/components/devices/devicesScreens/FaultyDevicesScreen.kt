package com.diabdata.ui.components.devices.devicesScreens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.MedicalDeviceInfoType
import com.diabdata.ui.components.ColoredIconCircle
import com.diabdata.ui.components.devices.components.ButtonType
import com.diabdata.ui.components.devices.components.FaultyToggleButton
import com.diabdata.ui.components.devices.components.MedicalDeviceCardData
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.darken
import com.diabdata.utils.getItemShape
import com.diabdata.utils.shortenedFormatLocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun FaultyDevices(
    dataViewModel: DataViewModel
) {
    val faultyDevices by dataViewModel.faultyDevices.collectAsState(initial = emptyList())
    val reportedFaultyDevices by dataViewModel.reportedFaultyDevices.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    FaultyDevicesScreen(
        faultyDevices = faultyDevices,
        reportedFaultyDevices = reportedFaultyDevices,
        onMarkAsReported = { device ->
            coroutineScope.launch {
                dataViewModel.updateDevice(device.copy(isReported = !device.isReported))
            }
        },
        onMarkAsFaulty = { device ->
            coroutineScope.launch {
                dataViewModel.updateDevice(device.copy(isFaulty = !device.isFaulty))
            }
        }
    )
}


@Composable
fun FaultyDevicesScreen(
    faultyDevices: List<MedicalDeviceEntry>,
    reportedFaultyDevices: List<MedicalDeviceEntry>,
    onMarkAsReported: (MedicalDeviceEntry) -> Unit,
    onMarkAsFaulty: (MedicalDeviceEntry) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        if (faultyDevices.isEmpty() && reportedFaultyDevices.isEmpty()) {
            Text(
                text = stringResource(R.string.device_screen_no_faulty_devices_tab),
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (faultyDevices.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.device_screen_faulty_devices_tab_faulty_heading),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.surfaceTint
                    )

                    DisplayCard(
                        items = faultyDevices,
                        onMarkFaulty = onMarkAsFaulty,
                        onMarkAsReported = onMarkAsReported
                    )
                }

                if (reportedFaultyDevices.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.device_screen_faulty_devices_tab_reported_heading),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.surfaceTint
                    )
                    DisplayCard(
                        items = reportedFaultyDevices,
                        onMarkFaulty = onMarkAsFaulty,
                        onMarkAsReported = onMarkAsReported
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FaultyDevicesScreenPreview() {
    val sampleDevices = listOf(
        MedicalDeviceEntry(
            date = LocalDate.now(),
            lifeSpanEndDate = LocalDate.now().plusDays(10),
            name = "Pompe à insuline",
            batchNumber = "1234A",
            serialNumber = "SN001",
            manufacturer = "Acme",
            deviceType = MedicalDeviceInfoType.WIRED_PUMP,
            createdAt = LocalDate.now(),
            isArchived = false,
            lifeSpan = 365,
            isFaulty = true,
            isReported = false,
            isLifeSpanOver = false,
            updatedAt = LocalDate.now(),
            referenceNumber = "REF001"
        ),
        MedicalDeviceEntry(
            date = LocalDate.now(),
            lifeSpanEndDate = LocalDate.now().plusDays(5),
            name = "Quickserter",
            batchNumber = "5678B",
            serialNumber = "SN002",
            manufacturer = "Acme",
            deviceType = MedicalDeviceInfoType.WIRED_PATCH,
            createdAt = LocalDate.now(),
            isArchived = false,
            lifeSpan = 365,
            isFaulty = false,
            isReported = true,
            isLifeSpanOver = false,
            updatedAt = LocalDate.now(),
            referenceNumber = "REF002"
        )
    )

    FaultyDevicesScreen(
        faultyDevices = sampleDevices,
        reportedFaultyDevices = sampleDevices.filter { it.isReported },
        onMarkAsReported = {},
        onMarkAsFaulty = {}
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DisplayCard(
    items: List<MedicalDeviceEntry>,
    onMarkFaulty: (MedicalDeviceEntry) -> Unit = {},
    onMarkAsReported: (MedicalDeviceEntry) -> Unit = {}
) {
    val cards = items.map { device ->
        MedicalDeviceCardData(
            device = device,
            titleText = device.name.ifEmpty { device.deviceType.displayName(LocalContext.current) },
            textColor = if (device.isLifeSpanOver) MaterialTheme.colorScheme.error else device.deviceType.baseColor,
            addableType = AddableType.DEVICE,
            isLifeSpanOver = device.isLifeSpanOver,
            iconRes = device.deviceType.iconRes,
            date = device.date,
            lifeSpanEndDate = device.lifeSpanEndDate,
            lifeSpan = device.lifeSpan,
            batchNumber = device.batchNumber
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        cards.forEachIndexed { index, card ->
            val animatedContainerColor by animateColorAsState(
                targetValue = if (card.device.isFaulty) {
                    MaterialTheme.colorScheme.error
                } else {
                    card.textColor.copy(alpha = 0.2f)
                },
                animationSpec = tween(
                    durationMillis = 500,
                    easing = androidx.compose.animation.core.EaseInOut
                ),
                label = "iconContainerColor"
            )

            val animatedReportedContainerColor by animateColorAsState(
                targetValue = if (card.device.isReported) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                },
                animationSpec = tween(
                    durationMillis = 500,
                    easing = androidx.compose.animation.core.EaseInOut
                ),
                label = "iconContainerColor"
            )

            val animatedIconColor by animateColorAsState(
                targetValue = if (card.device.isFaulty) {
                    MaterialTheme.colorScheme.onError
                } else {
                    card.textColor.darken()
                },
                animationSpec = tween(
                    durationMillis = 500,
                    easing = androidx.compose.animation.core.EaseInOut
                ),
                label = "iconContentColor"
            )

            val animatedReportIconColor by animateColorAsState(
                targetValue = if (card.device.isReported) {
                    MaterialTheme.colorScheme.onError
                } else {
                    MaterialTheme.colorScheme.onSurface.darken()
                },
                animationSpec = tween(
                    durationMillis = 500,
                    easing = androidx.compose.animation.core.EaseInOut
                ),
                label = "iconContentColor"
            )

            Surface(
                shape = getItemShape(index, cards.size),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColoredIconCircle(
                        iconRes = card.iconRes,
                        baseColor = if (card.textColor != primaryColor) card.textColor else card.addableType.baseColor,
                        size = 40.dp,
                        iconSize = 25.dp
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = card.titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = card.textColor.darken(),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                val space = 4.dp

                                if (card.batchNumber.isNotBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(space),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SvgIcon(
                                            resId = R.drawable.lot_icon_vector,
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = card.batchNumber,
                                            style = MaterialTheme.typography.bodyMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!card.device.referenceNumber.isNullOrBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(space),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SvgIcon(
                                            resId = R.drawable.ref_icon_vector,
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = card.device.referenceNumber,
                                            style = MaterialTheme.typography.bodyMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (!card.device.serialNumber.isNullOrBlank()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(space),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SvgIcon(
                                            resId = R.drawable.sn_icon_vector,
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = card.device.serialNumber,
                                            style = MaterialTheme.typography.bodyMediumEmphasized,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                FaultyToggleButton(
                                    isFaulty = card.device.isFaulty,
                                    isReported = card.device.isReported,
                                    onClick = { onMarkFaulty(card.device) },
                                    animatedContainerColor = animatedContainerColor,
                                    animatedIconColor = animatedIconColor,
                                )

                                FaultyToggleButton(
                                    isFaulty = card.device.isFaulty,
                                    isReported = card.device.isReported,
                                    type = ButtonType.REPORT,
                                    onClick = { onMarkAsReported(card.device) },
                                    animatedContainerColor = animatedReportedContainerColor,
                                    animatedIconColor = animatedReportIconColor,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = shortenedFormatLocalDate(card.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}