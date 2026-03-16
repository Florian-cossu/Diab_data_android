package com.diabdata.ui.components.applicationSettings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.shared.R as shared

@Composable
fun ChangelogDialog(onDismiss: () -> Unit) {
    rememberScrollState()
    val confirmButtonText = stringResource(shared.string.action_close)
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val maxHeight = with(density) { windowInfo.containerSize.height.toDp() * 0.45f }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onDismiss() })
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { }
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SvgIcon(
                        resId = shared.drawable.breaking_new_icon_vector,
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Updates - 16/03/2026",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxHeight)
                ) {
                    changelogSection(
                        "- BRANDING",
                        listOf(
                            "Created brand new application logo."
                        )
                    )
                    changelogSection(
                        "- CLEANUP",
                        listOf(
                            "Major reorganization of string resources with consistent naming.",
                            "Moved almost all existing app resources into shared.",
                            "Cleaned, grouped, and simplified libs.versions.toml.",
                            "Updated Gradle configuration across modules."
                        )
                    )
                    changelogSection(
                        "- GUI",
                        listOf(
                            "Turned stacked card into a component with pagination options",
                            "Updated the app to get closer to what native Google apps look like",
                            "Major reorganization of string resources with consistent naming.",
                            "(WIP) App is now adaptative to screen. Some components still need to be updated",
                        )
                    )
                    changelogSection(
                        "- DATABASE",
                        listOf(
                            "Added missing migration files.",
                            "Enabled schema export.",
                            "Fixed previous incorrect migrations."
                        )
                    )
                    changelogSection(
                        "- WEAR OS WATCHES SUPPORT",
                        listOf(
                            "Added wearOS module",
                            "Created new complications for expiring treatments, devices as well as upcoming appointments"
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(confirmButtonText, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}