package com.diabdata.ui.components.latestMeasurements.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diabdata.R
import com.diabdata.models.Treatment
import com.diabdata.models.TreatmentType
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class TreatmentCardData(
    val titleText: String,
    val dateText: String,
    val icon: Int,
    val isExpiringSoon: Boolean,
    val type: String
)

@DrawableRes
fun TreatmentType.iconRes(): Int = when (this) {
    TreatmentType.FAST_ACTING_RAPID_CARTRIDGE,
    TreatmentType.SLOW_ACTING_RAPID_CARTRIDGE -> R.drawable.cartridge_icon_vector

    TreatmentType.FAST_ACTING_RAPID_SYRINGE,
    TreatmentType.SLOW_ACTING_RAPID_SYRINGE,
    TreatmentType.GLUCAGON_SYRINGE -> R.drawable.syringe_icon_vector

    TreatmentType.FAST_ACTING_RAPID_VIAL,
    TreatmentType.SLOW_ACTING_RAPID_VIAL -> R.drawable.vial_icon_vector

    TreatmentType.GLUCAGON_SPRAY -> R.drawable.nasal_spray_icon_vector
}

@Composable
fun UpcomingTreatmentExpirationDates(
    treatments: List<Treatment>
) {
    val context = LocalContext.current
    if (treatments.isEmpty()) return

    val today = LocalDate.now()
    val soonThreshold = today.plusDays(30)

    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    val cards = treatments.sortedBy { it.expirationDate }
        .map { treatment ->
            TreatmentCardData(
                titleText = treatment.name.ifBlank { treatment.type.displayName(context) },
                dateText = "Expire le ${treatment.expirationDate.format(formatter)}",
                icon = treatment.type.iconRes(),
                isExpiringSoon = treatment.expirationDate.isBefore(soonThreshold),
                type = treatment.type.displayName(context)
            )
        }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Mes traitements",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Spacer(Modifier.height(8.dp))

        cards.forEachIndexed { index, card ->
            Surface(
                shape = getItemShape(index, cards.size),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SvgIcon(
                        resId = card.icon,
                        modifier = Modifier.size(26.dp),
                        color = if (card.isExpiringSoon) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = card.titleText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (card.isExpiringSoon)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            fontWeight = FontWeight.SemiBold,
                            text = card.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = card.dateText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (index != cards.size - 1) {
                Spacer(Modifier.height(3.dp))
            }
        }
    }
}
