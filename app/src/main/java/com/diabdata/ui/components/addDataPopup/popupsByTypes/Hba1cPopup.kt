package com.diabdata.ui.components.addDataPopup.popupsByTypes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.data.converters.toEntity
import com.diabdata.models.AddableType
import com.diabdata.models.HBA1CEntry
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.date_components.DateSelector
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun Hba1cPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    toUpdate: DataViewModel.MixedDbEntry.Hba1cEntry? = null
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    val scope = rememberCoroutineScope()

    var hba1cText by remember { mutableStateOf(toUpdate?.value?.toString() ?: "") }
    var selectedDate by remember { mutableStateOf(toUpdate?.date ?: LocalDate.now()) }

    val isValid = hba1cText.replace(',', '.').toFloatOrNull()?.let { it in 0f..15f } == true

    BasePopupLayout(
        title = context.getString(
            if (toUpdate == null) R.string.add_data_popup_title else R.string.update_data_popup_title,
            AddableType.HBA1C.getDisplayName(context)
        ),
        icon = AddableType.HBA1C.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            hba1cText.replace(',', '.').toFloatOrNull()?.let { value ->
                val entry = DataViewModel.MixedDbEntry.Hba1cEntry(
                    id = toUpdate?.id ?: 0,
                    date = selectedDate,
                    createdAt = toUpdate?.createdAt ?: today,
                    isArchived = toUpdate?.isArchived ?: false,
                    value = value,
                    updatedAt = today,
                    addableType = AddableType.HBA1C,
                    icon = AddableType.HBA1C.iconRes
                )

                if (toUpdate == null) {
                    dataViewModel.addHba1c(entry.toEntity() as HBA1CEntry)
                } else {
                    scope.launch {
                        dataViewModel.updateEntry(entry)
                    }
                }
            }
            onDismiss()
        },
        isConfirmEnabled = isValid
    ) {
        DateSelector(
            date = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        OutlinedTextField(
            value = hba1cText,
            onValueChange = { hba1cText = it },
            label = { Text(stringResource(R.string.add_data_popup_HBA1C_field_placeholder)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )
    }
}