package com.diabdata.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.ui.utils.SvgIcon

@Composable
fun SettingsScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = "Données",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.surfaceTint
            )

            Spacer(Modifier.height(8.dp))

            // Section
            Surface(
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsButton(
                        text = "Exporter les données",
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        icon = "backup_db_vector"
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingsButton(
                        text = "Importer des données",
                        onClick = { /* TODO */ },
                        shape = RectangleShape,
                        icon = "restore_db_vector"
                    )
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                    SettingsButton(
                        text = "Vider la base de données",
                        onClick = { /* TODO */ },
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
                        isDestructive = true,
                        icon = "purge_db_vector"
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    shape: Shape,
    isDestructive: Boolean = false,
    icon: String? = null
) {
    TextButton(
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (isDestructive) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show icon if provided
            icon?.takeIf { it.isNotBlank() }?.let {
                SvgIcon(
                    name = it,
                    modifier = Modifier.size(25.dp),
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}