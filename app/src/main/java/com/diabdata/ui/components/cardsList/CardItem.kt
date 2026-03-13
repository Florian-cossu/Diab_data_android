package com.diabdata.ui.components.cardsList

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.diabdata.ui.components.ColoredIconCircleProps

data class CardItem(
    val leadingIcon: Int? = null,
    val leadingIconColor: Color? = null,
    val leadingColoredCircleIcon: ColoredIconCircleProps? = null,
    val isDestructive: Boolean = false,
    val content: @Composable () -> Unit,
    val trailingIcon: Int? = null,
    val onClick: (() -> Unit)? = null,
    val onTrailingIconClick: (() -> Unit)? = null,
    val switchState: Boolean? = null,
    val onSwitchChange: ((Boolean) -> Unit)? = null
)