package com.diabdata.ui.components.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UserAvatarWithMenu(
    firstName: String?,
    lastName: String?,
    profilePhotoPath: String?,
    onEditProfile: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box (
        modifier = Modifier.padding(
            end = 32.dp
        )
    ) {
        UserAvatar(
            firstName = firstName,
            lastName = lastName,
            profilePhotoPath = profilePhotoPath,
            onClick = { menuExpanded = true }
        )
        UserAvatarMenu(
            expanded = menuExpanded,
            onDismiss = { menuExpanded = false },
            onEditProfile = onEditProfile
        )
    }
}