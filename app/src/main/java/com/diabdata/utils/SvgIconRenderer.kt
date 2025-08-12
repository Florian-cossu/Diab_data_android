package com.diabdata.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun SvgIcon(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = null, // ou une description si besoin
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(color)
    )
}
