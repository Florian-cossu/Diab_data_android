package com.diabdata.ui.components.applicationSettings

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.work.WorkManager
import com.diabdata.BuildConfig
import com.diabdata.data.DataViewModel
import com.diabdata.ui.components.applicationSettings.components.ChangelogDialog
import com.diabdata.ui.components.cardsList.CardItem
import com.diabdata.ui.components.cardsList.CardsList
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.showNotification
import com.diabdata.workers.reminders.scheduleAppointmentReminders
import com.diabdata.workers.reminders.scheduleMedicationExpirationReminders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import com.diabdata.shared.R as shared

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun SettingsScreen(dataViewModel: DataViewModel) {
    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val fileName = "diabdata_export_$currentDate.zip"
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val versionName = BuildConfig.VERSION_NAME
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

    val nextAppointmentDate by dataViewModel.upcomingAppointment
        .map { appointments ->
            appointments.minByOrNull { it.date }?.date
        }
        .collectAsState(initial = null)

    val nextTreatmentExpirationDate by dataViewModel.upcomingExpiringTreatmentDates
        .map { treatments ->
            treatments.minByOrNull { it.expirationDate }?.expirationDate
        }
        .collectAsState(initial = null)

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
            val dataBaseSection: List<CardItem> = listOf(
                CardItem(
                    leadingIcon = shared.drawable.backup_db_icon_vector,
                    content = {
                        Row {
                            Text(stringResource(shared.string.settings_export_data))
                        }
                    },
                    onClick = { createFileLauncher.launch(fileName) },
                    trailingIcon = shared.drawable.arrow_right_icon_vector
                ),
                CardItem(
                    leadingIcon = shared.drawable.restore_db_icon_vector,
                    content = {
                        Row {
                            Text(stringResource(shared.string.settings_import_data))
                        }
                    },
                    onClick = {
                        importFileLauncher.launch(
                            arrayOf(
                                "application/json",
                                "application/zip"
                            )
                        )
                    },
                    trailingIcon = shared.drawable.arrow_right_icon_vector
                ),
                CardItem(
                    leadingIcon = shared.drawable.purge_db_icon_vector,
                    leadingIconColor = MaterialTheme.colorScheme.error,
                    isDestructive = true,
                    content = {
                        Row {
                            Text(stringResource(shared.string.settings_purge_database))
                        }
                    },
                    onClick = { showConfirmDialog = true },
                    trailingIcon = shared.drawable.arrow_right_icon_vector
                )
            )

            val toastExpirationEnabled =
                stringResource(shared.string.toast_expiration_reminders_enabled)
            val toastAppointmentReminderEnabled =
                stringResource(shared.string.toast_appointment_reminders_enabled)

            val notificationSection: List<CardItem> = listOf(
                CardItem(
                    leadingIcon = shared.drawable.medication_expiry_notification_icon_vector,
                    content = {
                        val displayText = if (nextTreatmentExpirationDate != null) stringResource(
                            shared.string.settings_notification_next_expiration_reminder,
                            nextTreatmentExpirationDate!!.format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                            )
                        ) else ""

                        Column {
                            Text(
                                text = stringResource(shared.string.notification_expiration_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    switchState = enableExpirationDateReminder,
                    onSwitchChange = { isChecked ->
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
                            Toast.makeText(context, toastExpirationEnabled, Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            workManager.cancelAllWorkByTag("treatments")
                        }
                    },
                    trailingIcon = shared.drawable.notification_filled_icon_vector
                ),
                CardItem(
                    leadingIcon = shared.drawable.event_notification_icon_vector,
                    content = {
                        val displayText = if (nextAppointmentDate != null) stringResource(
                            shared.string.settings_notification_next_appointment_reminder,
                            nextAppointmentDate!!.format(
                                DateTimeFormatter.ofLocalizedDateTime(
                                    FormatStyle.MEDIUM,
                                    FormatStyle.SHORT
                                )
                            )
                        ) else ""

                        Column {
                            Text(
                                text = stringResource(shared.string.settings_notification_appointment),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = displayText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    switchState = enableAppointmentReminder,
                    onSwitchChange = { isChecked ->
                        enableAppointmentReminder = isChecked
                        prefs.edit { putBoolean("appointment_reminder", isChecked) }
                        val workManager = WorkManager.getInstance(context)
                        if (isChecked) {
                            scope.launch {
                                scheduleAppointmentReminders(
                                    context,
                                    dataViewModel
                                )
                            }
                            Toast.makeText(
                                context,
                                toastAppointmentReminderEnabled,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            workManager.cancelAllWorkByTag("appointments")
                        }
                    },
                    trailingIcon = shared.drawable.notification_filled_icon_vector
                )
            )

            val aboutApplicationSection: List<CardItem> = listOf(
                CardItem(
                    leadingIcon = shared.drawable.app_version_icon_vector,
                    content = {
                        Row {
                            Text("Diabdata $versionName")
                        }
                    },
                    onClick = { showChangeLogDialog = true },
                    trailingIcon = shared.drawable.arrow_right_icon_vector
                ),
                CardItem(
                    leadingIcon = shared.drawable.medication_info_icon_vector,
                    content = {
                        Row {
                            Text("Medication information file version $medicationsGtinFileversion")
                        }
                    },
                ),
                CardItem(
                    leadingIcon = shared.drawable.medical_device_info_version_icon_vector,
                    content = {
                        Row {
                            Text("Medical devices information file version $medicalDeviceGtinFileVersion")
                        }
                    },
                )
            )

            // Database section
            CardsList(
                header = stringResource(shared.string.settings_section_data),
                cards = dataBaseSection
            )

            // Notification section
            CardsList(
                header = stringResource(shared.string.settings_section_notifications),
                cards = notificationSection
            )

            // About app section
            CardsList(
                header = stringResource(shared.string.settings_section_application),
                cards = aboutApplicationSection
            )
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