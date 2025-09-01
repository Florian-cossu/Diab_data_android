package com.diabdata.ui.components.addDataPopup.popupsByTypes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import com.diabdata.models.WeightEntry
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.addDataPopup.getPopupTitleIcon
import com.diabdata.ui.components.date_components.DateSelector
import java.time.LocalDate

@Composable
fun WeightPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var weightText by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val isValid = weightText.replace(',', '.').toDoubleOrNull()?.let { it in 0.0..600.0 } == true

    BasePopupLayout(
        title = context.getString(
            R.string.add_data_popup_title,
            AddableType.WEIGHT.getDisplayName(context)
        ),
        icon = getPopupTitleIcon(AddableType.WEIGHT),
        onDismiss = onDismiss,
        onConfirm = {
            weightText.replace(',', '.').toFloatOrNull()?.let { value ->
                dataViewModel.addWeight(
                    WeightEntry(
                        date = selectedDate,
                        value = value,
                        isArchived = false,
                        createdAt = today
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
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text(stringResource(R.string.add_data_popup_weight_field_placeholder)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}