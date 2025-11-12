package com.diabdata.ui.components.addDataPopup.popupsByTypes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.data.converters.toEntity
import com.diabdata.models.AddableType
import com.diabdata.models.MedicalDeviceEntry
import com.diabdata.models.MedicalDeviceInfoType
import com.diabdata.ui.components.EnumDropdown
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.date_components.DateSelector
import com.diabdata.ui.components.layout.SvgIcon
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.diabdata.shared.R as shared

@Composable
fun MedicalDevicePopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    prefilled: MedicalDeviceEntry? = null,
    toUpdate: DataViewModel.MixedDbEntry.DeviceEntry? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()

    var name by remember { mutableStateOf(prefilled?.name ?: toUpdate?.name ?: "") }
    var manufacturer by remember {
        mutableStateOf(
            prefilled?.manufacturer ?: toUpdate?.manufacturer ?: ""
        )
    }
    var serialNumber by remember {
        mutableStateOf(
            prefilled?.serialNumber ?: toUpdate?.serialNumber ?: ""
        )
    }
    var batchNumber by remember {
        mutableStateOf(
            prefilled?.batchNumber ?: toUpdate?.batchNumber ?: ""
        )
    }
    var referenceNumber by remember {
        mutableStateOf(
            prefilled?.referenceNumber ?: toUpdate?.referenceNumber ?: ""
        )
    }
    var selectedDate by remember {
        mutableStateOf(
            prefilled?.date ?: toUpdate?.date ?: today
        )
    }

    var selectedDeviceType by remember {
        mutableStateOf(
            toUpdate?.deviceType ?: prefilled?.deviceType ?: MedicalDeviceInfoType.WIRED_PATCH
        )
    }
    var lifeSpan by remember { mutableIntStateOf(prefilled?.lifeSpan ?: toUpdate?.lifeSpan ?: 3) }
    var lifeSpanEndDate by remember {
        mutableStateOf(
            prefilled?.lifeSpanEndDate ?: toUpdate?.lifeSpanEndDate
            ?: today.plusDays(lifeSpan.toLong())
        )
    }
    var isFaulty by remember { mutableStateOf(prefilled?.isFaulty ?: toUpdate?.isFaulty ?: false) }
    var isReported by remember {
        mutableStateOf(
            prefilled?.isReported ?: toUpdate?.isReported ?: false
        )
    }
    var isLifeSpanOver by remember {
        mutableStateOf(
            prefilled?.isLifeSpanOver ?: toUpdate?.isLifeSpanOver ?: false
        )
    }

    val isValid = batchNumber.isNotBlank() && name.isNotBlank()

    BasePopupLayout(
        title = context.getString(
            if (toUpdate == null) R.string.add_data_popup_title else R.string.update_data_popup_title,
            AddableType.DEVICE.getDisplayName(context)
        ),
        icon = AddableType.DEVICE.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            val deviceEntry = DataViewModel.MixedDbEntry.DeviceEntry(
                id = toUpdate?.id ?: 0,
                date = selectedDate,
                lifeSpanEndDate = lifeSpanEndDate,
                addableType = AddableType.DEVICE,
                name = name,
                deviceType = selectedDeviceType,
                icon = AddableType.DEVICE.iconRes,
                isArchived = toUpdate?.isArchived ?: false,
                createdAt = toUpdate?.createdAt ?: today,
                updatedAt = today,
                batchNumber = batchNumber,
                serialNumber = serialNumber,
                referenceNumber = referenceNumber,
                manufacturer = manufacturer,
                lifeSpan = lifeSpan,
                isFaulty = isFaulty,
                isReported = isReported,
                isLifeSpanOver = isLifeSpanOver,
            )
            if (toUpdate == null) {
                dataViewModel.insertDevice(deviceEntry.toEntity() as MedicalDeviceEntry)
            } else {
                scope.launch {
                    dataViewModel.updateEntry(deviceEntry)
                }
            }
            onDismiss()
        },
        isConfirmEnabled = isValid
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // optionnel, un petit espace entre les colonnes
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                DateSelector(
                    date = selectedDate,
                    onDateSelected = {
                        selectedDate = it
                        lifeSpanEndDate = it.plusDays(lifeSpan.toLong())
                    },
                    isStartDate = true
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                DateSelector(
                    date = lifeSpanEndDate,
                    onDateSelected = { lifeSpanEndDate = it },
                    isEndDate = true
                )
            }
        }

        EnumDropdown(
            label = context.getString(R.string.add_data_popup_device_dropdown_placeholder),
            options = MedicalDeviceInfoType.entries,
            selected = selectedDeviceType,
            displayName = { it.displayName(context) },
            onSelectedChange = { selectedDeviceType = it },
            iconRes = { it.iconRes }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.add_data_popup_device_name_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = batchNumber,
            onValueChange = { batchNumber = it },
            label = { Text(stringResource(R.string.add_data_popup_device_batch_number_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = serialNumber,
            onValueChange = { serialNumber = it },
            label = { Text(stringResource(R.string.add_data_popup_device_serial_number_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = manufacturer,
            onValueChange = { manufacturer = it },
            label = { Text(stringResource(R.string.add_data_popup_device_manufacturer_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        OutlinedTextField(
            value = lifeSpan.toString(),
            onValueChange = {
                lifeSpan = it.toIntOrNull() ?: 0
                lifeSpanEndDate = selectedDate.plusDays(lifeSpan.toLong())
            },
            label = { Text(stringResource(R.string.add_data_popup_device_life_span_field_label)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )

        CreateToggle(
            text = context.getString(R.string.add_data_popup_device_is_faulty_toggle_label),
            displayText = context.getString(R.string.add_data_popup_device_is_faulty_toggle_display_text),
            checked = isFaulty,
            onCheckedChange = { isFaulty = it },
            icon = shared.drawable.faulty_medical_device_icon_vector,
            isDestructive = true
        )

        CreateToggle(
            text = context.getString(R.string.add_data_popup_device_is_reported_toggle_label),
            displayText = context.getString(R.string.add_data_popup_device_is_reported_toggle_display_text),
            checked = isReported,
            onCheckedChange = { isReported = it },
            icon = shared.drawable.report_icon_vector,
        )
    }
}

@Composable
fun CreateToggle(
    text: String,
    displayText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: Int? = null,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
            if (displayText.isNotBlank()) {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { isChecked ->
                onCheckedChange(isChecked)
            },
            thumbContent = {
                if (checked && icon != null) {
                    SvgIcon(
                        resId = icon,
                        contentDescription = null,
                        modifier = Modifier.size(SwitchDefaults.IconSize),
                        color = if (isDestructive) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            },
            colors =
                if (isDestructive) {
                    SwitchDefaults.colors().copy(
                        checkedThumbColor = MaterialTheme.colorScheme.error,
                        checkedTrackColor = MaterialTheme.colorScheme.errorContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.error,
                        uncheckedTrackColor = MaterialTheme.colorScheme.errorContainer,
                        uncheckedBorderColor = MaterialTheme.colorScheme.error,
                    )
                } else {
                    SwitchDefaults.colors()
                }
        )
    }
}