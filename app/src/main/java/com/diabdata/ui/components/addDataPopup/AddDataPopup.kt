package com.diabdata.ui.components.addDataPopup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Treatment
import com.diabdata.ui.components.addDataPopup.popupsByTypes.AppointmentPopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.Hba1cPopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.ImportantDatePopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.TreatmentPopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.WeightPopup
import com.diabdata.utils.SvgIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDataPopup(
    type: AddableType,
    dataViewModel: DataViewModel,
    onDismiss: () -> Unit,
    prefilledTreatment: Treatment? = null
) {
    when (type) {
        AddableType.APPOINTMENT -> AppointmentPopup(onDismiss, dataViewModel)
        AddableType.TREATMENT -> TreatmentPopup(onDismiss, dataViewModel, prefilledTreatment)
        AddableType.WEIGHT -> WeightPopup(onDismiss, dataViewModel)
        AddableType.HBA1C -> Hba1cPopup(onDismiss, dataViewModel)
        AddableType.IMPORTANT_DATE -> ImportantDatePopup(onDismiss, dataViewModel)
    }
}

@Composable
fun BasePopupLayout(
    title: String,
    icon: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isConfirmEnabled: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SvgIcon(resId = icon, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(title.uppercase(), color = MaterialTheme.colorScheme.primary)
                }

                content()

                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            stringResource(R.string.cancel_button_text),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = isConfirmEnabled
                    ) {
                        Text(stringResource(R.string.confirm_button_text))
                    }
                }
            }
        }
    }
}