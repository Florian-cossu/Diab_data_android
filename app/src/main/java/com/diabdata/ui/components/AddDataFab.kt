package com.diabdata.ui.components

import android.annotation.SuppressLint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.models.AddableType
import com.diabdata.utils.SvgIcon

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun AddDataFab(
    onSelect: (AddableType) -> Unit,
    onScanClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (expanded) 45f else 0f)
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    val fabItems = listOf(
        Triple("Scanner un médicament", R.drawable.data_matrix_icon_vector) { onScanClick() },
        Triple(
            stringResource(R.string.addable_weight),
            R.drawable.weight_add_icon_vector
        ) { onSelect(AddableType.WEIGHT) },
        Triple(stringResource(R.string.addable_hba1c), R.drawable.hba1c_add_icon_vector) {
            onSelect(
                AddableType.HBA1C
            )
        },
        Triple(
            stringResource(R.string.addable_diagnosis),
            R.drawable.diagnosis_icon_vector
        ) { onSelect(AddableType.DIAGNOSIS) },
        Triple(
            stringResource(R.string.addable_treatment),
            R.drawable.medication_add_icon_vector
        ) { onSelect(AddableType.TREATMENT) },
        Triple(
            stringResource(R.string.addable_appointment),
            R.drawable.event_add_icon_vector
        ) { onSelect(AddableType.APPOINTMENT) }
    )

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.95f))
                    .clickable { expanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            // On ne crée les items que si expanded = true
            if (expanded) {
                fabItems.reversed().forEachIndexed { index, (label, iconRes, action) ->

                    var startAnimation by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        startAnimation = true
                    }

                    val offsetX by animateDpAsState(
                        targetValue = if (startAnimation) 0.dp else screenWidth,
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = index * 70,
                            easing = FastOutSlowInEasing
                        )
                    )

                    val alpha by animateFloatAsState(
                        targetValue = if (startAnimation) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 400,
                            delayMillis = index * 70
                        )
                    )

                    FabOption(
                        label = label,
                        iconRes = iconRes,
                        onClick = {
                            expanded = false
                            action()
                        },
                        modifier = Modifier
                            .offset(x = offsetX)
                            .alpha(alpha)
                    )
                }
            }

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter",
                    modifier = Modifier.rotate(rotation),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun FabOption(
    label: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clickable { onClick() }
            .padding(end = 4.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(30.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            SvgIcon(
                iconRes,
                Modifier.size(18.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}