package com.diabdata.ui.components.applicationSettings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.diabdata.BuildConfig
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.ui.components.applicationSettings.components.ChangelogDialog
import com.diabdata.ui.components.applicationSettings.components.SettingsButton
import com.diabdata.ui.components.applicationSettings.components.SettingsSection
import com.diabdata.ui.components.applicationSettings.components.SettingsToggle
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.showNotification
import com.diabdata.workers.scheduleAppointmentReminders
import com.diabdata.workers.scheduleMedicationExpirationReminders
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import com.diabdata.shared.R as shared

@Composable
fun SettingsScreen(dataViewModel: DataViewModel) {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val fileName = "diabdata_export_$currentDate.json"
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val versionName = BuildConfig.VERSION_NAME
    val versionCode = BuildConfig.VERSION_CODE
    val medicationsGtinFileversion = BuildConfig.MEDICATION_GTIN_FILE_VERSION
    val medicalDeviceGtinFileVersion = BuildConfig.MEDICAL_DEVICES_GTIN_FILE_VERSION

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
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 3.dp,
                        bottomEnd = 3.dp
                    ),
                    icon = shared.drawable.backup_db_icon_vector
                )
                SettingsButton(
                    text = stringResource(R.string.settings_page_data_import_button_text),
                    onClick = { importFileLauncher.launch(arrayOf("application/json")) },
                    shape = RoundedCornerShape(3.dp),
                    icon = shared.drawable.restore_db_icon_vector
                )
                SettingsButton(
                    text = stringResource(R.string.settings_page_data_purge_button_text),
                    onClick = { showConfirmDialog = true },
                    shape = RoundedCornerShape(
                        topStart = 3.dp,
                        topEnd = 3.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    isDestructive = true,
                    icon = shared.drawable.purge_db_icon_vector
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
                    icon = shared.drawable.notification_active_icon_vector,
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
                    icon = shared.drawable.notification_active_icon_vector,
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
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = 3.dp,
                        bottomEnd = 3.dp
                    ),
                    icon = shared.drawable.app_version_icon_vector
                )
                SettingsButton(
                    text = "Medication information file version $medicationsGtinFileversion",
                    onClick = { },
                    shape = RoundedCornerShape(3.dp),
                    icon = shared.drawable.medication_info_icon_vector
                )
                SettingsButton(
                    text = "Medical devices information file version $medicalDeviceGtinFileVersion",
                    onClick = { },
                    shape = RoundedCornerShape(
                        topStart = 3.dp,
                        topEnd = 3.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    icon = shared.drawable.medical_device_info_version_icon_vector
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
                    resId = shared.drawable.purge_db_icon_vector,
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
        ChangelogDialog(onDismiss = { showChangeLogDialog = false })
    }
}