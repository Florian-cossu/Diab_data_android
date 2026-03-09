package com.diabdata.ui.components.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UserAvatarWithMenu(
    firstName: String?,
    lastName: String?,
    profilePhotoPath: String?,
    onEditProfile: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
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

@Preview(showBackground = true, name = "Avatar basic")
@Composable
fun UserAvatarWithMenuPreview() {
    UserAvatarWithMenu(
        firstName = "John",
        lastName = "Doe",
        profilePhotoPath = null,
        onEditProfile = {}
    )
}