package com.diabdata.ui.components.addDataPopup.popupsByTypes

import androidx.compose.foundation.layout.fillMaxWidth
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
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Treatment
import com.diabdata.models.TreatmentType
import com.diabdata.ui.components.EnumDropdown
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.addDataPopup.getPopupTitleIcon
import com.diabdata.ui.components.date_components.DateSelector
import java.time.LocalDate

@Composable
fun TreatmentPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel,
    prefilled: Treatment? = null
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var name by remember { mutableStateOf(prefilled?.name ?: "") }
    var selectedDate by remember { mutableStateOf(prefilled?.expirationDate ?: LocalDate.now()) }
    var selectedTreatmentType by remember {
        mutableStateOf(
            prefilled?.type ?: TreatmentType.FAST_ACTING_INSULIN_VIAL
        )
    }

    BasePopupLayout(
        title = context.getString(
            R.string.add_data_popup_title,
            AddableType.TREATMENT.getDisplayName(context)
        ),
        icon = getPopupTitleIcon(AddableType.TREATMENT),
        onDismiss = onDismiss,
        onConfirm = {
            dataViewModel.addTreatment(
                Treatment(
                    expirationDate = selectedDate,
                    name = name,
                    type = selectedTreatmentType,
                    isArchived = false,
                    createdAt = today
                )
            )
            onDismiss()
        },
        isConfirmEnabled = name.isNotBlank()
    ) {
        DateSelector(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            isExpiryDate = true
        )

        EnumDropdown(
            label = context.getString(R.string.add_data_popup_medication_dropdown_placeholder),
            options = TreatmentType.entries,
            selected = selectedTreatmentType,
            displayName = { it.displayName(context) },
            onSelectedChange = { selectedTreatmentType = it }
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.add_data_popup_medication_field_placeholder)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}