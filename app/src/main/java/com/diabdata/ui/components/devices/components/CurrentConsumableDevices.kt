package com.diabdata.ui.components.devices.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.MedicalDeviceInfoType
import com.diabdata.ui.components.ColoredIconCircle
import com.diabdata.ui.components.layout.FaultyToggleButton
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.darken
import com.diabdata.utils.getItemShape
import com.diabdata.utils.shortenedFormatLocalDate
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun CurrentConsumableDevicesList(viewModel: DataViewModel) {
    val currentDevices by viewModel.currentConsumableDevices.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()

    val filteredDevices = currentDevices
        .groupBy { it.deviceType }
        .mapValues { (_, devices) ->
            devices.sortedByDescending { it.date }
        }
        .flatMap { (_, devices) ->
            val latest = devices.first()
            val redundant = devices.drop(1).filterNot { old ->
                old.lifeSpanEndDate.isEqual(latest.date)
            }
            listOf(latest) + redundant
        }

    val showSection = filteredDevices.isNotEmpty()

    if (showSection) {
        CurrentConsumableDevicesCards(
            filteredDevices,
            onMarkFaulty = { device ->
                coroutineScope.launch {
                    viewModel.updateDevice(
                        device.copy(
                            isFaulty = !device.isFaulty
                        )
                    )
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CurrentConsumableDevicesCards(
    devices: List<MedicalDeviceEntry>,
    onMarkFaulty: (MedicalDeviceEntry) -> Unit = {},
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    if (devices.isEmpty()) return

    val today = LocalDate.now()

    val cards = devices.map { device ->
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

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.current_consumable_devices_card_section_heading),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(Modifier.height(8.dp))

        cards.forEachIndexed { index, card ->
            // Color animation for isFaulty device button
            val animatedContainerColor by animateColorAsState(
                targetValue = if (card.device.isFaulty) {
                    card.textColor.darken()
                } else {
                    card.textColor.copy(alpha = 0.2f)
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
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
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

                            FaultyToggleButton(
                                isFaulty = card.device.isFaulty,
                                isReported = card.device.isReported,
                                onClick = { onMarkFaulty(card.device) },
                                animatedContainerColor = animatedContainerColor,
                                animatedIconColor = animatedIconColor
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (card.lifeSpanEndDate.isEqual(today)) {
                                SvgIcon(
                                    resId = R.drawable.warning_icon_vector,
                                    color = MedicalDeviceInfoType.WIRELESS_PATCH.baseColor,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = stringResource(R.string.current_non_consumable_devices_card_replacement_warning_text),
                                    color = card.device.deviceType.baseColor,
                                )
                            } else {
                                Text(
                                    text = shortenedFormatLocalDate(card.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                // Progress bar
                                val totalDays = card.lifeSpan
                                val usedDays =
                                    ChronoUnit.DAYS.between(card.date, today).coerceAtLeast(0)
                                val progress = (usedDays.toFloat() / totalDays).coerceIn(0f, 1f)

                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.weight(1f),
                                    color = if (card.isLifeSpanOver) MaterialTheme.colorScheme.error else card.textColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = shortenedFormatLocalDate(card.lifeSpanEndDate),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            if (index != cards.size - 1) Spacer(Modifier.height(3.dp))
        }
    }
}

@Preview(
    showBackground = true, locale = "en", showSystemUi = false,
    wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun ConsumableDevicesListPreview() {
    val fakeData = listOf(
        MedicalDeviceEntry(
            id = 1,
            date = LocalDate.of(2025, 9, 1),
            lifeSpanEndDate = LocalDate.of(2025, 9, 1).plusDays(90),
            name = "Dexcom G6 Transmitter",
            batchNumber = "D378HP",
            serialNumber = "IUVYZBE678",
            referenceNumber = "UTIUFYEGI",
            manufacturer = "Dexcom",
            deviceType = MedicalDeviceInfoType.CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER,
            createdAt = LocalDate.of(2025, 9, 1),
            isArchived = false,
            lifeSpan = 90,
            isFaulty = false,
            isReported = false,
            isLifeSpanOver = false,
            updatedAt = LocalDate.of(2025, 9, 1)
        ),
        MedicalDeviceEntry(
            id = 2,
            date = LocalDate.of(2025, 9, 21),
            lifeSpanEndDate = LocalDate.of(2025, 9, 21).plusDays(3),
            name = "Omnipod Pod",
            batchNumber = "567GGU",
            serialNumber = "UTYFYZ38",
            referenceNumber = "1R5TFG",
            manufacturer = "Insulet",
            deviceType = MedicalDeviceInfoType.WIRELESS_PATCH,
            createdAt = LocalDate.of(2025, 9, 25),
            isArchived = false,
            lifeSpan = 3,
            isFaulty = false,
            isReported = false,
            isLifeSpanOver = false,
            updatedAt = LocalDate.of(2025, 9, 1)
        ),
        MedicalDeviceEntry(
            id = 2,
            date = LocalDate.of(2025, 9, 24),
            lifeSpanEndDate = LocalDate.of(2025, 9, 24).plusDays(3),
            name = "Omnipod Pod",
            batchNumber = "567GGU",
            serialNumber = "UTYFYZ38",
            referenceNumber = "1R5TFG",
            manufacturer = "Insulet",
            deviceType = MedicalDeviceInfoType.WIRELESS_PATCH,
            createdAt = LocalDate.of(2025, 9, 25),
            isArchived = false,
            lifeSpan = 3,
            isFaulty = true,
            isReported = false,
            isLifeSpanOver = false,
            updatedAt = LocalDate.of(2025, 9, 1)
        )
    )

    val filteredFakeData = fakeData
        .groupBy { it.deviceType }
        .mapValues { (_, devices) ->
            devices.sortedByDescending { it.date }
        }
        .flatMap { (_, devices) ->
            val latest = devices.first()
            val redundant = devices.drop(1).filterNot { old ->
                old.lifeSpanEndDate.isEqual(latest.date)
            }
            listOf(latest) + redundant
        }

    MaterialTheme {
        Column {
            CurrentConsumableDevicesCards(filteredFakeData)
        }
    }
}