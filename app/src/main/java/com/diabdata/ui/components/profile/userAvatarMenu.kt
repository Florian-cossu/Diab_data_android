package com.diabdata.ui.components.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.shared.R as R

@Composable
fun UserAvatarMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEditProfile: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = 0.dp, y = 8.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.widthIn(min = 250.dp)
    ) {
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
        DropdownMenuItem(
            leadingIcon = {
                SvgIcon(
                    resId = R.drawable.cast_to_desktop_icon_vector,
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = { Text(stringResource(R.string.cast_to_user_computer)) },
            onClick = {
                onDismiss()
            }
        )
    }
}