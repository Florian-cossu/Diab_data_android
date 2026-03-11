package com.diabdata.ui.components.applicationSettings

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import com.diabdata.data.DataViewModel
import com.diabdata.ui.components.applicationSettings.components.ChangelogDialog
import com.diabdata.ui.components.applicationSettings.components.SettingsButton
import com.diabdata.ui.components.applicationSettings.components.SettingsSection
import com.diabdata.ui.components.applicationSettings.components.SettingsToggle
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.showNotification
import com.diabdata.workers.reminders.scheduleAppointmentReminders
import com.diabdata.workers.reminders.scheduleMedicationExpirationReminders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import com.diabdata.shared.R as shared

@Composable
fun SettingsScreen(dataViewModel: DataViewModel) {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val fileName = "diabdata_export_$currentDate.zip"
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

    val notifChannelName = stringResource(shared.string.notification_channel_data)
    val dataExportSuccess = stringResource(shared.string.toast_data_export_success)
    val dataImportSuccess = stringResource(shared.string.toast_data_import_success)
    val dataExportError = stringResource(shared.string.toast_data_export_error)
    val dataImportError = stringResource(shared.string.toast_data_import_error)
    val emptyImportFileError = stringResource(shared.string.toast_empty_file_error)

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
        onResult = { uri: Uri? ->
            uri?.let {
                val channelName = notifChannelName
                val successText = dataExportSuccess
                val errorText = dataExportError
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        ZipOutputStream(outputStream).use { zip ->
                            // 1. Export JSON
                            val jsonString = dataViewModel.exportDataAsJsonString()
                            zip.putNextEntry(ZipEntry("data.json"))
                            zip.write(jsonString.toByteArray())
                            zip.closeEntry()

                            // 2. Export profile pic if it exists
                            dataViewModel.userDetails.value?.profilePhotoPath?.let { path ->
                                val photoFile = File(path)
                                if (photoFile.exists()) {
                                    zip.putNextEntry(ZipEntry("profile_photo.jpg"))
                                    photoFile.inputStream().use { it.copyTo(zip) }
                                    zip.closeEntry()
                                }
                            }
                        }
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
        }
    )

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                val channelName = notifChannelName
                val successText = dataImportSuccess
                val errorText = dataImportError
                val errorEmptyFile = emptyImportFileError

                scope.launch(Dispatchers.IO) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val bytes = inputStream.readBytes()
                            Log.d("Import", "1. File read: ${bytes.size} bytes")

                            val isZip = bytes.size >= 2
                                    && bytes[0] == 0x50.toByte()
                                    && bytes[1] == 0x4B.toByte()

                            if (isZip) {
                                var jsonString: String? = null
                                var photoBytes: ByteArray? = null

                                // 1. Lire TOUT le ZIP
                                ZipInputStream(bytes.inputStream()).use { zip ->
                                    var entry = zip.nextEntry
                                    while (entry != null) {
                                        when (entry.name) {
                                            "data.json" -> jsonString = String(zip.readBytes())
                                            "profile_photo.jpg" -> photoBytes = zip.readBytes()
                                        }
                                        zip.closeEntry()
                                        entry = zip.nextEntry
                                    }
                                }

                                // 2. D'abord sauvegarder la photo
                                var newPhotoPath: String? = null
                                photoBytes?.let { pBytes ->
                                    val photoFile = File(
                                        context.filesDir,
                                        "profile_photo_${System.currentTimeMillis()}.jpg"
                                    )
                                    photoFile.outputStream().use { output ->
                                        output.write(pBytes)
                                    }
                                    context.filesDir.listFiles()
                                        ?.filter {
                                            it.name.startsWith("profile_photo")
                                                    && it.name != photoFile.name
                                        }
                                        ?.forEach { it.delete() }

                                    newPhotoPath = photoFile.absolutePath
                                }

                                // 3. Ensuite importer le JSON
                                jsonString?.let { json ->
                                    if (json.isNotEmpty()) {
                                        dataViewModel.importDataFromJsonString(json)
                                    }
                                }

                                // 4. Enfin mettre à jour le path photo en DB après un délai
                                //    pour s'assurer que le upsert du JSON est terminé
                                newPhotoPath?.let { path ->
                                    delay(500)  // Attendre que le viewModelScope.launch termine
                                    dataViewModel.updateProfilePhotoPath(path)
                                }
                            } else {
                                // Ancien format JSON
                                val jsonString = String(bytes)
                                if (jsonString.isNotEmpty()) {
                                    dataViewModel.importDataFromJsonString(jsonString)
                                } else {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, errorEmptyFile, Toast.LENGTH_LONG).show()
                                    }
                                    return@use
                                }
                            }

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, successText, Toast.LENGTH_SHORT).show()
                            }
                            context.showNotification(
                                title = successText,
                                content = uri.lastPathSegment.orEmpty(),
                                channelName = channelName,
                            )
                        }
                    } catch (e: Exception) {
                        Log.e("Import", "GLOBAL CRASH", e)
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "$errorText : ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    )

    val workManager = WorkManager.getInstance(context)

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                title = stringResource(shared.string.settings_section_data)
            ) {
                SettingsButton(
                    text = stringResource(shared.string.settings_export_data),
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
                    text = stringResource(shared.string.settings_import_data),
                    onClick = { importFileLauncher.launch(arrayOf("application/json", "application/zip")) },
                    shape = RoundedCornerShape(3.dp),
                    icon = shared.drawable.restore_db_icon_vector
                )
                SettingsButton(
                    text = stringResource(shared.string.settings_purge_database),
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
                title = stringResource(shared.string.settings_section_notifications)
            ) {
                SettingsToggle(
                    text = stringResource(shared.string.notification_expiration_title),
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
                    icon = shared.drawable.notification_filled_icon_vector,
                    toastText = stringResource(shared.string.toast_expiration_reminders_enabled),
                    nextReminderDate = nextTreatmentReminder
                )
                SettingsToggle(
                    text = stringResource(shared.string.settings_notification_appointment),
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
                    icon = shared.drawable.notification_filled_icon_vector,
                    toastText = stringResource(shared.string.toast_appointment_reminders_enabled),
                    nextReminderDate = nextAppointmentReminder
                )
            }

            // Section Application
            SettingsSection(
                title = stringResource(shared.string.settings_section_application)
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
        val purgeDatabaseModalTitle = stringResource(shared.string.dialog_purge_title)
        val purgeDatabaseModalContents = stringResource(shared.string.dialog_purge_message)
        val confirmButtonText = stringResource(shared.string.action_confirm)
        val cancelButtonText = stringResource(shared.string.action_cancel)

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