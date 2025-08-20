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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.BuildConfig
import com.diabdata.R
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
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE

    var showConfirmDialog by remember { mutableStateOf(false) }

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri: Uri? ->
            uri?.let {
                val jsonString = dataViewModel.exportDataAsJsonString()

                val channelName = context.getString(R.string.notification_channel_name_data)
                val successText = context.getString(R.string.data_export_success_text)
                val errorText = context.getString(R.string.data_export_error_text)

                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(context, successText, Toast.LENGTH_SHORT).show()
                    context.showNotification(
                        title = successText,
                        content = uri.lastPathSegment.orEmpty(),
                        notificationChannel = channelName,
                        notificationDescription = ""
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    context.showNotification(
                        title = "$errorText : ${e.message}",
                        content = uri.lastPathSegment.orEmpty(),
                        notificationChannel = channelName,
                        notificationDescription = ""
                    )
                }
            }
        }
    )

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val channelName = context.getString(R.string.notification_channel_name_data)
                val successText = context.getString(R.string.data_import_success_text)
                val errorText = context.getString(R.string.data_import_error_text)
                val errorEmptyFile = context.getString(R.string.empty_file_error_text)

                try {
                    val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()
                        ?.use { reader ->
                            reader.readText()
                        }

                    if (!jsonString.isNullOrEmpty()) {
                        dataViewModel.importDataFromJsonString(jsonString)

                        Toast.makeText(context, successText, Toast.LENGTH_SHORT).show()
                        context.showNotification(
                            title = successText,
                            content = uri.lastPathSegment.orEmpty(),
                            notificationChannel = channelName,
                            notificationDescription = ""
                        )
                    } else {
                        Toast.makeText(context, errorEmptyFile, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    context.showNotification(
                        title = "$errorText : ${e.message}",
                        content = uri.lastPathSegment.orEmpty(),
                        notificationChannel = channelName,
                        notificationDescription = ""
                    )
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
                text = stringResource(R.string.settings_page_data_heading),
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
                        text = stringResource(R.string.settings_page_data_export_button_text),
                        onClick = { createFileLauncher.launch(fileName) },
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = 3.dp,
                            bottomEnd = 3.dp
                        ),
                        icon = R.drawable.backup_db_icon_vector
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    SettingsButton(
                        text = stringResource(R.string.settings_page_data_import_button_text),
                        onClick = {
                            importFileLauncher.launch(arrayOf("application/json"))
                        },
                        shape = RoundedCornerShape(3.dp),
                        icon = R.drawable.restore_db_icon_vector
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    SettingsButton(
                        text = stringResource(R.string.settings_page_data_purge_button_text),
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
                        icon = R.drawable.purge_db_icon_vector
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.settings_page_application_heading),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.surfaceTint
            )

            Spacer(Modifier.height(8.dp))

            Column {
                SettingsButton(
                    text = "Version $versionName (code: $versionCode)",
                    onClick = { },
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    icon = R.drawable.app_version_icon_vector
                )
            }
        }
    }

    // Dialog de confirmation
    if (showConfirmDialog) {
        val purgeDatabaseModalTitle = stringResource(R.string.database_data_purge_modal_title)
        val purgeDatabaseModalContents = stringResource(R.string.database_data_purge_modal_contents)
        val confirmButtonText = stringResource(R.string.confirm_button_text)
        val cancelButtonText = stringResource(R.string.cancel_button_text)

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(purgeDatabaseModalTitle) },
            text = { Text(purgeDatabaseModalContents) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dataViewModel.clearDatabase()
                        showConfirmDialog = false
                    }
                ) {
                    Text(confirmButtonText, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text(cancelButtonText)
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
    icon: Int = 0
) {
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
                    .padding(start = 15.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SvgIcon(
                    resId = icon,
                    modifier = Modifier.size(25.dp),
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}