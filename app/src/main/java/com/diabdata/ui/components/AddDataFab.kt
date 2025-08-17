package com.diabdata.ui.components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.models.AddableType
import com.diabdata.utils.SvgIcon

@Composable
fun AddDataFab(onSelect: (AddableType) -> Unit) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    Box {
        FloatingActionButton(onClick = {expanded = !expanded}) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(context.getString(R.string.addable_weight)) },
                onClick = {
                    expanded = false
                    onSelect(AddableType.WEIGHT)
                },
                leadingIcon = {
                    SvgIcon(
                        resId = (R.drawable.weight_add_icon_vector),
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(context.getString(R.string.addable_hba1c)) },
                onClick = {
                    expanded = false
                    onSelect(AddableType.HBA1C)
                },
                leadingIcon = {
                    SvgIcon(
                        resId = (R.drawable.hba1c_add_icon_vector),
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(context.getString(R.string.addable_diagnosis)) },
                onClick = {
                    expanded = false
                    onSelect(AddableType.DIAGNOSIS)
                },
                leadingIcon = {
                    SvgIcon(
                        resId = (R.drawable.diagnosis_icon_vector),
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(context.getString(R.string.addable_treatment)) },
                onClick = {
                    expanded = false
                    onSelect(AddableType.TREATMENT)
                },
                leadingIcon = {
                    SvgIcon(
                        resId = (R.drawable.medication_add_icon_vector),
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            DropdownMenuItem(
                text = { Text(context.getString(R.string.addable_appointment)) },
                onClick = {
                    expanded = false
                    onSelect(AddableType.APPOINTMENT)
                },
                leadingIcon = {
                    SvgIcon(
                        resId = (R.drawable.event_add_icon_vector),
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}