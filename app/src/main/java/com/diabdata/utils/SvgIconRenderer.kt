package com.diabdata.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SvgIcon(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier,
    color: Color? = null,
    contentDescription: String? = null
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = color?.let { ColorFilter.tint(it) })
}

@Composable
fun GradientAiIcon(
    painter: Painter,
    modifier: Modifier = Modifier.size(24.dp),
    gradient: Brush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )
) {
    Canvas(modifier = modifier) {
        drawIntoCanvas { canvas ->
            val paint = androidx.compose.ui.graphics.Paint()
            canvas.saveLayer(bounds = size.toRect(), paint = paint)

            with(painter) { draw(size) }
            drawRect(
                brush = gradient,
                size = size,
                blendMode = BlendMode.SrcIn
            )

            canvas.restore()
        }
    }
}