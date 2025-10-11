package com.diabdata.ui.components.applicationSettings.components

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.ui.components.layout.SvgIcon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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