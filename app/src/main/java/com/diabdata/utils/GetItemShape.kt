package com.diabdata.utils

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

fun getItemShape(index: Int, size: Int): Shape {
    if (size == 1) {
        return RoundedCornerShape(16.dp)
    }
    return when (index) {
        0 -> RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 3.dp,
            bottomEnd = 3.dp
        )

        size - 1 -> RoundedCornerShape(
            topStart = 3.dp,
            topEnd = 3.dp,
            bottomStart = 16.dp,
            bottomEnd = 16.dp
        )

        else -> androidx.compose.foundation.shape.RoundedCornerShape(3.dp)
    }
}