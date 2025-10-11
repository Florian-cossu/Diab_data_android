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
import com.diabdata.R
import com.diabdata.ui.components.layout.SvgIcon

@Composable
fun ChangelogDialog(onDismiss: () -> Unit) {
    rememberScrollState()
    val confirmButtonText = stringResource(R.string.confirm_button_text)
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
            shape = MaterialTheme.shapes.extraLarge, // Coins plus arrondis
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
                        resId = R.drawable.breaking_new_icon_vector,
                        modifier = Modifier.size(32.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Updates - 11/10/2025",
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
                        "- NEW SECTION",
                        listOf(
                            "Added devices section in Navbar",
                            "Added currently active consumable medical devices component",
                            "Added non consumable medical devices list component",
                            "Added tab navigation on device page to list reported and faulty devices"
                        )
                    )
                    changelogSection(
                        "- SETTINGS PAGE",
                        listOf(
                            "Added GTIN Csv version number in settings page",
                            "Updated clear database popup text"
                        )
                    )
                    changelogSection(
                        "- ICONS",
                        listOf("Added custom icon sets for upcoming device page")
                    )
                    changelogSection(
                        "- DATABASE MANAGEMENT",
                        listOf(
                            "Added clean database migrations",
                            "Added medical devices database",
                            "Added medical device insertion popup",
                            "Added medical device scan popup",
                            "Added medical device to the flow feeding the database view page"
                        )
                    )
                    changelogSection(
                        "- DATAMATRIX PARSER",
                        listOf("Reworked datamatrix parser to be more robust")
                    )
                    changelogSection(
                        "- WIDGET IMPLEMENTATION",
                        listOf(
                            "Finished Glance widget implementation",
                            "Finished preview of widget for launcher"
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