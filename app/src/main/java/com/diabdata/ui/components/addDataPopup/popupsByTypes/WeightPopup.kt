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
import com.diabdata.data.DataViewModel
import com.diabdata.data.converters.toEntity
import com.diabdata.models.WeightEntry
import com.diabdata.shared.utils.dataTypes.AddableType
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.date_components.DateSelector
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.diabdata.shared.R as shared

@Composable
fun WeightPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    toUpdate: DataViewModel.MixedDbEntry.WeightEntry? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()

    var weightText by remember {
        mutableStateOf(
            toUpdate?.value?.toString() ?: ""
        )
    }
    var selectedDate by remember {
        mutableStateOf(toUpdate?.date ?: LocalDate.now())
    }

    val isValid = weightText.replace(',', '.').toDoubleOrNull()?.let { it in 0.0..600.0 } == true

    BasePopupLayout(
        title = context.getString(
            if (toUpdate == null) shared.string.popup_title_add else shared.string.popup_title_update,
            AddableType.WEIGHT.getDisplayName(context)
        ),
        icon = AddableType.WEIGHT.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            weightText.replace(',', '.').toFloatOrNull()?.let { value ->
                val entry = DataViewModel.MixedDbEntry.WeightEntry(
                    id = toUpdate?.id ?: 0,
                    date = selectedDate,
                    addableType = AddableType.WEIGHT,
                    value = value,
                    icon = AddableType.WEIGHT.iconRes,
                    isArchived = toUpdate?.isArchived ?: false,
                    createdAt = toUpdate?.createdAt ?: today,
                    updatedAt = today
                )
                if (toUpdate == null) {
                    dataViewModel.addWeight(entry.toEntity() as WeightEntry)
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
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text(stringResource(shared.string.popup_placeholder_weight)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )
    }
}