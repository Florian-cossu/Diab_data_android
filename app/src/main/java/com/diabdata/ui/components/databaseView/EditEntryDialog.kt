package com.diabdata.ui.components.databaseView

import androidx.compose.runtime.Composable
import com.diabdata.data.DataViewModel
import com.diabdata.ui.components.addDataPopup.popupsByTypes.AppointmentPopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.Hba1cPopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.ImportantDatePopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.TreatmentPopup
import com.diabdata.ui.components.addDataPopup.popupsByTypes.WeightPopup

@Composable
fun EditEntryDialog(
    entry: DataViewModel.MixedDbEntry,
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel
) {
    when (entry) {
        is DataViewModel.MixedDbEntry.AppointmentEntry -> {
            AppointmentPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.TreatmentEntry -> {
            TreatmentPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.WeightEntry -> {
            WeightPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.Hba1cEntry -> {
            Hba1cPopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }

        is DataViewModel.MixedDbEntry.ImportantDateEntry -> {
            ImportantDatePopup(
                onDismiss = onDismiss,
                dataViewModel = dataViewModel,
                toUpdate = entry
            )
        }
    }
}