package com.diabdata.ui.components.profile

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.diabdata.castServer.castToUser.CastToUserServerService
import com.diabdata.shared.R
import com.diabdata.ui.components.layout.SvgIcon

// ui/UserAvatarMenu.kt
@Composable
fun UserAvatarMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit
) {
    val context = LocalContext.current

    var isServerRunning by remember { mutableStateOf(CastToUserServerService.isRunning) }

    LaunchedEffect(expanded) {
        if (expanded) {
            isServerRunning = CastToUserServerService.isRunning
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = 0.dp, y = 8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.widthIn(min = 250.dp)
    ) {
        // --- Edit profile ---
        DropdownMenuItem(
            leadingIcon = {
                SvgIcon(
                    resId = R.drawable.edit_user_icon_vector,
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = { Text(stringResource(R.string.edit_profile)) },
            onClick = {
                onDismiss()
                onEditProfile()
            }
        )

        // --- Cast to doctor ---
        DropdownMenuItem(
            leadingIcon = {
                SvgIcon(
                    resId = R.drawable.secure_cast_to_desktop_icon_vector,
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = { Text(stringResource(R.string.cast_to_doctors_computer)) },
            onClick = {
                onDismiss()
            }
        )

        // --- Cast to my computer ---
        DropdownMenuItem(
            leadingIcon = {
                SvgIcon(
                    resId = if (isServerRunning)
                        R.drawable.stop_icon_vector
                    else
                        R.drawable.cast_to_desktop_icon_vector,
                    modifier = Modifier.size(24.dp),
                    color = if (isServerRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                if (isServerRunning) {
                    Text(
                        text = stringResource(R.string.stop_casting),
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Text(text = stringResource(R.string.cast_to_user_computer))
                }
            },
            onClick = {
                if (isServerRunning) {
                    CastToUserServerService.stop(context)
                    isServerRunning = false
                } else {
                    CastToUserServerService.start(context)
                    isServerRunning = true
                }
                onDismiss()
            }
        )
    }
}