package com.diabdata.ui.components.applicationSettings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.diabdata.BuildConfig
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.showNotification
import com.diabdata.workers.scheduleAppointmentReminders
import com.diabdata.workers.scheduleMedicationExpirationReminders
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
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
    val medicationsGtinFileversion = BuildConfig.GTIN_FILE_VERSION

    val scope = rememberCoroutineScope()

    var showConfirmDialog by remember { mutableStateOf(false) }
    var showChangeLogDialog by remember { mutableStateOf(false) }

    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var enableExpirationDateReminder by remember {
        mutableStateOf(prefs.getBoolean("expiration_reminder", false))
    }
    var enableAppointmentReminder by remember {
        mutableStateOf(prefs.getBoolean("appointment_reminder", false))
    }

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
                        channelName = channelName,
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    context.showNotification(
                        title = "$errorText : ${e.message}",
                        content = uri.lastPathSegment.orEmpty(),
                        channelName = channelName,
                    )
                }
            }
        })

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(), onResult = { uri: Uri? ->
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
                            channelName = channelName,
                        )
                    } else {
                        Toast.makeText(context, errorEmptyFile, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    context.showNotification(
                        title = "$errorText : ${e.message}",
                        content = uri.lastPathSegment.orEmpty(),
                        channelName = channelName,
                    )
                }
            }
        })

    val workManager = WorkManager.getInstance(context)

// LiveData observables
    val appointmentInfos by workManager.getWorkInfosByTagLiveData("appointments")
        .observeAsState(initial = emptyList())
    val treatmentInfos by workManager.getWorkInfosByTagLiveData("treatments")
        .observeAsState(initial = emptyList())

    val nextAppointmentReminder = remember(appointmentInfos) {
        appointmentInfos
            .filter { it.state == WorkInfo.State.ENQUEUED }
            .mapNotNull { info ->
                info.tags.firstOrNull { it.startsWith("appointments_") }
                    ?.removePrefix("appointments_")
                    ?.toLongOrNull()
            }.minOfOrNull { epochMilli ->
                Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate()
            }
    }

    val nextTreatmentReminder = remember(treatmentInfos) {
        treatmentInfos
            .filter { it.state == WorkInfo.State.ENQUEUED }
            .mapNotNull { info ->
                info.tags.firstOrNull { it.startsWith("treatments_") }
                    ?.removePrefix("treatments_")
                    ?.toLongOrNull()
            }.minOfOrNull { epochMilli ->
                Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDate()
            }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp
                )
                .background(Color.Transparent)
                .verticalScroll(scrollState)
                .padding(20.dp),
            verticalArrangement = spacedBy(32.dp)
        ) {
            // Database section
            SettingsSection(
                title = stringResource(R.string.settings_page_data_heading)
            ) {
                SettingsButton(
                    text = stringResource(R.string.settings_page_data_export_button_text),
                    onClick = { createFileLauncher.launch(fileName) },
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    icon = R.drawable.backup_db_icon_vector
                )
                SettingsButton(
                    text = stringResource(R.string.settings_page_data_import_button_text),
                    onClick = { importFileLauncher.launch(arrayOf("application/json")) },
                    shape = RoundedCornerShape(3.dp),
                    icon = R.drawable.restore_db_icon_vector
                )
                SettingsButton(
                    text = stringResource(R.string.settings_page_data_purge_button_text),
                    onClick = { showConfirmDialog = true },
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    isDestructive = true,
                    icon = R.drawable.purge_db_icon_vector
                )
            }

            // Notification section
            SettingsSection(
                title = stringResource(R.string.settings_page_notifications_headings)
            ) {
                SettingsToggle(
                    text = stringResource(R.string.settings_page_notifications_expiration_date),
                    checked = enableExpirationDateReminder,
                    onCheckedChange = { isChecked ->
                        enableExpirationDateReminder = isChecked
                        prefs.edit { putBoolean("expiration_reminder", isChecked) }
                        val workManager = WorkManager.getInstance(context)
                        if (isChecked) {
                            scope.launch {
                                scheduleMedicationExpirationReminders(
                                    context,
                                    dataViewModel
                                )
                            }
                        } else {
                            workManager.cancelAllWorkByTag("treatments")
                        }
                    },
                    icon = R.drawable.notification_active_icon_vector,
                    toastText = stringResource(R.string.settings_page_notifications_expiration_date_confirmation_toast),
                    nextReminderDate = nextTreatmentReminder
                )
                SettingsToggle(
                    text = stringResource(R.string.settings_page_notifications_appointment),
                    checked = enableAppointmentReminder,
                    onCheckedChange = { isChecked ->
                        enableAppointmentReminder = isChecked
                        prefs.edit { putBoolean("appointment_reminder", isChecked) }
                        val workManager = WorkManager.getInstance(context)
                        if (isChecked) {
                            scope.launch {
                                scheduleAppointmentReminders(context, dataViewModel)
                            }
                        } else {
                            workManager.cancelAllWorkByTag("appointments")
                        }
                    },
                    icon = R.drawable.notification_active_icon_vector,
                    toastText = stringResource(R.string.settings_page_notifications_appointment_confirmation_toast),
                    nextReminderDate = nextAppointmentReminder
                )
            }

            // Section Application
            SettingsSection(
                title = stringResource(R.string.settings_page_application_heading)
            ) {
                SettingsButton(
                    text = "Diabdata $versionName (code: $versionCode)",
                    onClick = {
                        showChangeLogDialog = true
                    },
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    icon = R.drawable.app_version_icon_vector
                )
                SettingsButton(
                    text = "Medication information file version $medicationsGtinFileversion",
                    onClick = { },
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                    icon = R.drawable.medication_icon_vector
                )
            }
        }
    }

    if (showConfirmDialog) {
        val purgeDatabaseModalTitle = stringResource(R.string.database_data_purge_modal_title)
        val purgeDatabaseModalContents = stringResource(R.string.database_data_purge_modal_contents)
        val confirmButtonText = stringResource(R.string.confirm_button_text)
        val cancelButtonText = stringResource(R.string.cancel_button_text)

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            icon = {
                SvgIcon(
                    resId = R.drawable.purge_db_icon_vector,
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(purgeDatabaseModalTitle) },
            text = { Text(purgeDatabaseModalContents) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dataViewModel.clearDatabase(context)
                        showConfirmDialog = false
                    }) {
                    Text(confirmButtonText, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }) {
                    Text(cancelButtonText)
                }
            }
        )
    }
    if (showChangeLogDialog) {
        val confirmButtonText = stringResource(R.string.confirm_button_text)

        AlertDialog(
            onDismissRequest = { showChangeLogDialog = false },
            icon = {
                SvgIcon(
                    resId = R.drawable.breaking_new_icon_vector,
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Updates - 15/09/2025") },
            text = {
                LazyColumn {
                    item { Text("- New section") }
                    item { Text("\t• Added devices section in Navbar") }
                    item { Text("- Settings page") }
                    item { Text("\t• Added GTIN Csv version number in settings page") }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showChangeLogDialog = false }
                ) {
                    Text(confirmButtonText, color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.surfaceTint
        )

        Spacer(Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            tonalElevation = 0.dp,
            color = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(verticalArrangement = spacedBy(3.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsButton(
    text: String, onClick: () -> Unit, shape: Shape, isDestructive: Boolean = false, icon: Int = 0
) {
    Surface(
        shape = shape, tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = onClick, shape = shape, colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent,
                contentColor = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            ), modifier = Modifier.fillMaxWidth()
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
                    text, style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun SettingsToggle(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape = RoundedCornerShape(0.dp),
    icon: Int? = null,
    toastText: String = "",
    nextReminderDate: LocalDate?,
) {
    Surface(
        shape = shape,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val context = LocalContext.current

        val displayText = if (nextReminderDate != null) stringResource(
            R.string.settings_notification_toggle_next_reminder_date, nextReminderDate.format(
                DateTimeFormatter.ofLocalizedDate(
                    FormatStyle.MEDIUM
                )
            )
        ) else ""

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 30.dp, top = 10.dp, end = 15.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium
                )
                if (displayText.isNotBlank()) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = { isChecked ->
                    onCheckedChange(isChecked)
                    if (toastText.isNotBlank() && isChecked) {
                        Toast.makeText(
                            context,
                            toastText,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                thumbContent = {
                    if (checked && icon != null) {
                        SvgIcon(
                            resId = icon,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

            )
        }
    }
}
