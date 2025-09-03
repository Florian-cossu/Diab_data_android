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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.HBA1CEntry
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.addDataPopup.getPopupTitleIcon
import com.diabdata.ui.components.date_components.DateSelector
import java.time.LocalDate

@Composable
fun Hba1cPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var hba1cText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val isValid = hba1cText.replace(',', '.').toFloatOrNull()?.let { it in 0f..15f } == true

    BasePopupLayout(
        title = context.getString(
            R.string.add_data_popup_title,
            AddableType.HBA1C.getDisplayName(context)
        ),
        icon = getPopupTitleIcon(AddableType.HBA1C),
        onDismiss = onDismiss,
        onConfirm = {
            hba1cText.replace(',', '.').toFloatOrNull()?.let { value ->
                dataViewModel.addHba1c(
                    HBA1CEntry(
                        date = selectedDate,
                        createdAt = today,
                        isArchived = false,
                        value = value,
                        updatedAt = today
                    )
                )
            }
            onDismiss()
        },
        isConfirmEnabled = isValid
    ) {
        DateSelector(
            initialDate = selectedDate,
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