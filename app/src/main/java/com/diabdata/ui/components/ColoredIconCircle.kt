package com.diabdata.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.ui.darken

@Composable
fun ColoredIconCircle(
    iconRes: Int,
    baseColor: Color,
    size: Dp = 40.dp,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = baseColor.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        SvgIcon(
            resId = iconRes,
            modifier = Modifier.size(iconSize),
            color = baseColor.darken(0.2f) // extension pour assombrir
        )
    }
}