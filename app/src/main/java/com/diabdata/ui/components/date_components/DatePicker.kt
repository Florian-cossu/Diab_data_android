package com.diabdata.ui.components.date_components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.diabdata.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun DateSelector(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    isExpiryDate: Boolean = false,
    isStartDate: Boolean = false,
    isEndDate: Boolean = false
) {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    var dateText by remember { mutableStateOf(TextFieldValue(date.format(formatter))) }
    var showModal by remember { mutableStateOf(false) }

    LaunchedEffect(date) {
        // Sync externe -> interne
        dateText = TextFieldValue(date.format(formatter))
    }

    val labelText = if (isExpiryDate)
        stringResource(R.string.add_data_popup_expiration_date_field_placeholder)
    else if (isStartDate)
        stringResource(R.string.add_data_popup_start_date_field_placeholder)
    else if (isEndDate)
        stringResource(R.string.add_data_popup_end_date_field_placeholder)
    else
        stringResource(R.string.add_data_popup_date_field_placeholder)

    OutlinedTextField(
        value = dateText,
        onValueChange = { newValue ->
            dateText = newValue
            try {
                val parsedDate = LocalDate.parse(newValue.text, formatter)
                onDateSelected(parsedDate)
            } catch (_: DateTimeParseException) {
            }
        },
        label = { Text(labelText) },
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = labelText,
                modifier = Modifier.clickable { showModal = true }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { showModal = true },
        shape = MaterialTheme.shapes.small
    )

    if (showModal) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    val newDate = Instant.ofEpochMilli(it)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(newDate)
                }
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}

@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text(stringResource(R.string.confirm_button_text))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button_text))
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}