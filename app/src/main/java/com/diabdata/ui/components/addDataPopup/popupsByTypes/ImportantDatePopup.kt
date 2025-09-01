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
import com.diabdata.models.DiagnosisDate
import com.diabdata.ui.components.addDataPopup.BasePopupLayout
import com.diabdata.ui.components.addDataPopup.getPopupTitleIcon
import com.diabdata.ui.components.date_components.DateSelector
import java.time.LocalDate

@Composable
fun DiagnosisPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel
) {
    val context = LocalContext.current
    val today = LocalDate.now()

    var diagnosis by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    BasePopupLayout(
        title = context.getString(
            R.string.add_data_popup_title,
            AddableType.DIAGNOSIS.getDisplayName(context)
        ),
        icon = getPopupTitleIcon(AddableType.DIAGNOSIS),
        onDismiss = onDismiss,
        onConfirm = {
            dataViewModel.addDiagnosisDate(
                DiagnosisDate(
                    date = selectedDate,
                    diagnosis = diagnosis,
                    isArchived = false,
                    createdAt = today
                )
            )
            onDismiss()
        },
        isConfirmEnabled = diagnosis.isNotBlank()
    ) {
        DateSelector(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it }
        )

        OutlinedTextField(
            value = diagnosis,
            onValueChange = { diagnosis = it },
            label = { Text(stringResource(R.string.add_data_popup_diagnosis_field_placeholder)) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}