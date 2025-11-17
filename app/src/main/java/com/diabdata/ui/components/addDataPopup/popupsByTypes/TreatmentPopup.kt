package com.diabdata.ui.components.addDataPopup.popupsByTypes

import androidx.compose.foundation.layout.fillMaxWidth
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
import com.diabdata.data.DataViewModel
import com.diabdata.data.converters.toEntity
import com.diabdata.models.AddableType
import com.diabdata.models.Treatment
import com.diabdata.models.TreatmentType
import com.diabdata.ui.components.EnumDropdown
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.date_components.DateSelector
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.diabdata.shared.R as shared

@Composable
fun TreatmentPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    prefilled: Treatment? = null,
    toUpdate: DataViewModel.MixedDbEntry.TreatmentEntry? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val today = LocalDate.now()

    var name by remember { mutableStateOf(toUpdate?.name ?: prefilled?.name ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            toUpdate?.date ?: prefilled?.expirationDate ?: LocalDate.now()
        )
    }
    var selectedTreatmentType by remember {
        mutableStateOf(
            toUpdate?.treatmentType ?: prefilled?.type ?: TreatmentType.FAST_ACTING_INSULIN_VIAL
        )
    }

    BasePopupLayout(
        title = context.getString(
            if (toUpdate == null) shared.string.popup_title_add else shared.string.popup_title_update,
            AddableType.TREATMENT.getDisplayName(context)
        ),
        icon = AddableType.TREATMENT.iconRes,
        onDismiss = onDismiss,
        onConfirm = {
            val treatmentEntry = DataViewModel.MixedDbEntry.TreatmentEntry(
                id = toUpdate?.id ?: 0,
                date = selectedDate,
                addableType = AddableType.TREATMENT,
                name = name,
                treatmentType = selectedTreatmentType,
                icon = AddableType.TREATMENT.iconRes,
                isArchived = toUpdate?.isArchived ?: false,
                createdAt = toUpdate?.createdAt ?: today,
                updatedAt = today
            )
            if (toUpdate == null) {
                dataViewModel.addTreatment(treatmentEntry.toEntity() as Treatment)
            } else {
                scope.launch {
                    dataViewModel.updateEntry(treatmentEntry)
                }
            }
            onDismiss()
        },
        isConfirmEnabled = name.isNotBlank()
    ) {
        DateSelector(
            date = selectedDate,
            onDateSelected = { selectedDate = it },
            isExpiryDate = true
        )

        EnumDropdown(
            label = context.getString(shared.string.popup_placeholder_medication_type),
            options = TreatmentType.entries,
            selected = selectedTreatmentType,
            displayName = { it.displayName(context) },
            onSelectedChange = { selectedTreatmentType = it },
            iconRes = { it.iconRes }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(shared.string.popup_placeholder_medication_name)) },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )
    }
}