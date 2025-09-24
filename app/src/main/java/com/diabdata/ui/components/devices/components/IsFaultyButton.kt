package com.diabdata.ui.components.devices.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.utils.SvgIcon
import kotlinx.coroutines.delay

data class ButtonSize(val circle: Int, val icon: Int)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FaultyToggleButton(
    isFaulty: Boolean,
    onClick: () -> Unit,
    animatedContainerColor: Color,
    animatedIconColor: Color,
    modifier: Modifier = Modifier,
    sizing: ButtonSize = ButtonSize(30, 20)
) {
    var clicked by remember { mutableStateOf(false) }

    LaunchedEffect(clicked) {
        if (clicked) {
            delay(200)
            clicked = false
        }
    }

    IconButton(
        onClick = {
            clicked = true
            onClick()
        },
        shapes = IconButtonDefaults.shapes(
            shape = MaterialShapes.Square.toShape(),
            pressedShape = MaterialShapes.Cookie12Sided.toShape()
        ),
        modifier = modifier
            .size(sizing.circle.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = animatedContainerColor,
            contentColor = animatedIconColor
        ),
    ) {
        SvgIcon(
            resId = if (isFaulty)
                R.drawable.information_filled_icon_vector
            else R.drawable.information_icon_vector,
            modifier = Modifier.size(sizing.icon.dp),
            color = animatedIconColor
        )
    }
}