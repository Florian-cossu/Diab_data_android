package com.diabdata.ui.utils

import androidx.compose.foundation.Image
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
fun SvgIcon (name: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurface) {
    val context = LocalContext.current
    val resId = context.resources.getIdentifier(name,"drawable", context.packageName)

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Icon: $name",
            modifier = modifier,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(color)
        )
    } else {
        return
    }
}