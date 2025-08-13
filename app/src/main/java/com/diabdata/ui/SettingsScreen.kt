package com.diabdata.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.data.DataViewModel
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.showNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(dataViewModel: DataViewModel) {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val fileName = "diabdata_export_$currentDate.json"
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showConfirmDialog by remember { mutableStateOf(false) }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let {
                val jsonString = dataViewModel.exportDataAsJsonString()
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, "Export réussi", Toast.LENGTH_SHORT).show()
                    context.showNotification(
                        title = "Export des données effectué",
                        content = uri.lastPathSegment.orEmpty()
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Erreur lors de l'export : ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()
                        ?.use { reader ->
                            reader.readText()
                        }

                    if (!jsonString.isNullOrEmpty()) {
                        // Utiliser ta fonction dans ViewModel qui fait le parsing et met à jour les states
                        dataViewModel.importDataFromJsonString(jsonString)

                        Toast.makeText(context, "Import réussi", Toast.LENGTH_SHORT).show()
                        context.showNotification(
                            title = "Import des données effectué",
                            content = uri.lastPathSegment.orEmpty()
                        )
                    } else {
                        Toast.makeText(context, "Fichier vide", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Erreur lors de l'import : ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    )

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp, 32.dp, 32.dp, 0.dp)
                .background(Color.Transparent)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Données",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.surfaceTint
            )

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsButton(
                        text = "Exporter les données",
                        onClick = { createFileLauncher.launch(fileName) },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 3.dp,
                            bottomEnd = 3.dp
                        ),
                        icon = "backup_db_icon_vector"
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    SettingsButton(
                        text = "Importer des données",
                        onClick = {
                            importFileLauncher.launch(arrayOf("application/json"))
                        },
                        shape = RoundedCornerShape(3.dp),
                        icon = "restore_db_icon_vector"
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    SettingsButton(
                        text = "Vider la base de données",
                        onClick = {
                            showConfirmDialog = true
                        },
                        shape = RoundedCornerShape(
                            topStart = 3.dp,
                            topEnd = 3.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 16.dp
                        ),
                        isDestructive = true,
                        icon = "purge_db_icon_vector"
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Application",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.surfaceTint
            )

            Spacer(Modifier.height(8.dp))

            Column {
                SettingsButton(
                    text = "Version 1.0",
                    onClick = { },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    icon = "app_version_icon_vector"
                )
            }
        }
    }

    // Dialog de confirmation
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmer la suppression") },
            text = { Text("Êtes-vous sûr de vouloir vider complètement la base de données ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        dataViewModel.clearDatabase()
                        showConfirmDialog = false
                    }
                ) {
                    Text("Confirmer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    shape: Shape,
    isDestructive: Boolean = false,
    icon: String? = null
) {
    val context = LocalContext.current

    Surface(
        shape = shape,
        tonalElevation = 4.dp, // Tonal elevation sur le bouton
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = onClick,
            shape = shape,
            colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent, // On laisse la Surface gérer le fond
                contentColor = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.takeIf { it.isNotBlank() }?.let { iconName ->
                    val resId =
                        context.resources.getIdentifier(iconName, "drawable", context.packageName)
                    if (resId != 0) {
                        SvgIcon(
                            resId = resId,
                            modifier = Modifier.size(25.dp),
                            color = if (isDestructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }

                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}