package com.diabdata.ui.components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.diabdata.models.AddableType
import com.diabdata.ui.utils.SvgIcon

@Composable
fun AddDataFab(onSelect: (AddableType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FloatingActionButton(onClick = {expanded = !expanded}) {
            Icon(Icons.Default.Add, contentDescription = "Ajouter")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Poids") },
                onClick = {
                    expanded = false
                    onSelect(AddableType.WEIGHT)
                },
                leadingIcon = {
                    SvgIcon(name = "weight_add_icon_vector", modifier = Modifier.size(24.dp))
                }
            )
            DropdownMenuItem(
                text = { Text("HBA1C") },
                onClick = {
                    expanded = false
                    onSelect(AddableType.HBA1C)
                },
                leadingIcon = {
                    SvgIcon(name = "hba1c_edit_icon_vector", modifier = Modifier.size(24.dp))
                }
            )
            DropdownMenuItem(
                text = { Text("Traitement") },
                onClick = {
                    expanded = false
                    onSelect(AddableType.TREATMENT)
                },
                leadingIcon = {
                    SvgIcon(name = "medication_icon_vector", modifier = Modifier.size(24.dp))
                }
            )
            DropdownMenuItem(
                text = { Text("RDV") },
                onClick = {
                    expanded = false
                    onSelect(AddableType.APPOINTMENT)
                },
                leadingIcon = {
                    SvgIcon(name = "event_add_icon_vector", modifier = Modifier.size(24.dp))
                }
            )
        }
    }
}