package com.diabdata.feature.settings.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.diabdata.core.utils.ui.SvgIcon

@Composable
fun SettingsButton(
    text: String, onClick: () -> Unit, shape: Shape, isDestructive: Boolean = false, icon: Int = 0
) {
    Surface(
        color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(
            onClick = onClick, shape = shape, colors = ButtonDefaults.textButtonColors(
                containerColor = Color.Transparent,
                contentColor = if (isDestructive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            ), modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SvgIcon(
                    resId = icon,
                    modifier = Modifier.size(25.dp),
                    color = if (isDestructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text, style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}