package com.diabdata.ui.components.cardsList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.diabdata.ui.components.ColoredIconCircle
import com.diabdata.ui.components.ColoredIconCircleProps
import com.diabdata.ui.components.layout.SvgIcon
import com.diabdata.utils.ui.getItemShape

/**
 * A composable that renders a single card within a [CardsList] stack.
 *
 * The card layout follows a horizontal structure with up to four optional sections,
 * arranged from start to end:
 *
 * ```
 * ┌─────────────────────────────────────────────────────────┐
 * │  [Leading Icon]  [Content]  [Trailing Icon]  [Switch]   │
 * └─────────────────────────────────────────────────────────┘
 * ```
 *
 * ## Leading icon behavior
 * The leading section supports two mutually exclusive modes, resolved in priority order:
 * 1. **Colored circle icon** ([CardItem.leadingColoredCircleIcon]): an icon rendered inside
 *    a tinted circular background via [ColoredIconCircle].
 * 2. **Simple SVG icon** ([CardItem.leadingIcon]): a flat icon rendered via [SvgIcon]
 *    with [MaterialTheme.colorScheme.onSurfaceVariant] tint.
 * 3. If neither is provided, no leading section is displayed.
 *
 * ## Trailing section
 * - If [CardItem.trailingIcon] is set, an [IconButton] is displayed on the trailing side.
 *   Clicking it invokes [CardItem.onTrailingIconClick] if provided.
 * - If [CardItem.switchState] and [CardItem.onSwitchChange] are both set,
 *   a [Switch] is displayed after the trailing icon.
 * - Both trailing icon and switch can coexist on the same card.
 *
 * ## Styling
 * - The card uses [MaterialTheme.colorScheme.surfaceContainerHigh] as its background color.
 * - The [shape] parameter controls the corner rounding, typically computed by [getItemShape]
 *   based on the card's position within the parent [CardsList] stack.
 *
 * ## Usage example
 * ```kotlin
 * CardListItem(
 *     cardItem = CardItem(
 *         leadingColoredCircleIcon = ColoredIconCircleProps(
 *             iconRes = R.drawable.ic_sensor,
 *             baseColor = Color(0xFF4CAF50)
 *         ),
 *         content = {
 *             Column {
 *                 Text("Glucose sensor", style = MaterialTheme.typography.bodyLarge)
 *                 Text("Expires in 3 days", style = MaterialTheme.typography.bodySmall)
 *             }
 *         },
 *         trailingIcon = R.drawable.ic_chevron_right,
 *         onTrailingIconClick = { /* navigate */ }
 *     ),
 *     shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
 * )
 * ```
 *
 * @param cardItem  The [CardItem] data model describing the content and behavior of this card.
 * @param shape     The [Shape] applied to the card's corners. Typically provided by [getItemShape]
 *                  to ensure correct rounding based on position within the stack.
 * @param modifier  [Modifier] applied to the root [Card] container.
 *
 * @see CardItem                Data model describing the content of a single card.
 * @see CardsList               Parent composable that orchestrates the full card stack.
 * @see getItemShape            Utility that computes corner shapes based on position within the stack.
 * @see ColoredIconCircle       Composable rendering an icon inside a tinted circular background.
 * @see ColoredIconCircleProps  Data model for the colored circle icon configuration.
 * @see SvgIcon                 Composable rendering an SVG resource icon.
 */
@Composable
fun CardListItem(
    cardItem: CardItem,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = shape,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when {
                cardItem.leadingColoredCircleIcon != null -> {
                    ColoredIconCircle(
                        baseColor = cardItem.leadingColoredCircleIcon.baseColor,
                        iconRes = cardItem.leadingColoredCircleIcon.iconRes,
                        size = cardItem.leadingColoredCircleIcon.size,
                        iconSize = cardItem.leadingColoredCircleIcon.iconSize
                    )
                }

                cardItem.leadingIcon != null -> {
                    SvgIcon(
                        resId = cardItem.leadingIcon,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                cardItem.content()
            }

            if (cardItem.trailingIcon != null) {
                IconButton(
                    onClick = { cardItem.onTrailingIconClick?.invoke() },
                    modifier = Modifier.size(24.dp)
                ) {
                    SvgIcon(
                        resId = cardItem.trailingIcon,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            if (cardItem.switchState != null && cardItem.onSwitchChange != null) {
                Switch(
                    checked = cardItem.switchState,
                    onCheckedChange = cardItem.onSwitchChange
                )
            }
        }
    }
}