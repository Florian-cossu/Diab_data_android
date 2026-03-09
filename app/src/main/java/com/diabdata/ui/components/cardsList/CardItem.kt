package com.diabdata.ui.components.cardsList

import androidx.compose.runtime.Composable
import com.diabdata.ui.components.ColoredIconCircleProps

data class CardItem(
    val leadingIcon: Int? = null,
    val leadingColoredCircleIcon: ColoredIconCircleProps? = null,
    val content: @Composable () -> Unit,
    val trailingIcon: Int? = null,
    val onTrailingIconClick: (() -> Unit)? = null,
    val switchState: Boolean? = null,
    val onSwitchChange: ((Boolean) -> Unit)? = null
)