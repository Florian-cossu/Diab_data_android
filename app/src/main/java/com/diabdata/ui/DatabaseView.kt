package com.diabdata.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry
import com.diabdata.ui.components.FlippableSelectionIcon
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun DatabaseEditionView(
    dataViewModel: DataViewModel
) {
    var selectedTypes by remember { mutableStateOf(setOf<AddableType>()) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedEntries by remember { mutableStateOf(setOf<DbEntry>()) }

    val weights by dataViewModel.weights.collectAsState()
    val hba1cEntries by dataViewModel.hba1cEntries.collectAsState()
    val appointments by dataViewModel.appointments.collectAsState()
    val treatments by dataViewModel.treatments.collectAsState()
    val diagnosisDates by dataViewModel.diagnosis.collectAsState()

    val allEntries = mergeEntries(weights, hba1cEntries, appointments, treatments, diagnosisDates)

    val filteredEntries = if (selectedTypes.isEmpty()) allEntries
    else allEntries.filter { it.type in selectedTypes }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            FilterChips(
                types = AddableType.entries,
                selectedTypes = selectedTypes,
                onTypeToggle = { type ->
                    selectedTypes = if (selectedTypes.contains(type)) selectedTypes - type
                    else selectedTypes + type
                }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = selectionMode && selectedEntries.isNotEmpty(),
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    val interactionSource = remember { MutableInteractionSource() }
                    val isPressed by interactionSource.collectIsPressedAsState()
                    val isHovered by interactionSource.collectIsHoveredAsState()

                    val bgColor = if (isPressed || isHovered) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)

                    val textColor = if (isPressed || isHovered) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.error

                    val badgeColor = if (isPressed || isHovered) MaterialTheme.colorScheme.onError
                    else MaterialTheme.colorScheme.error

                    val badgeTextColor = if (isPressed || isHovered) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onError

                    Button(
                        onClick = {
                            selectedEntries.forEach {
                                dataViewModel.deleteEntry(it.id, it.type.tableName)
                            }
                            selectedEntries = emptySet()
                            selectionMode = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bgColor,
                            contentColor = textColor
                        ),
                        interactionSource = interactionSource
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = badgeColor) {
                                    Text(selectedEntries.size.toString(), color = badgeTextColor)
                                }
                            }
                        ) {
                            SvgIcon(
                                resId = R.drawable.delete_icon_vector,
                                color = textColor
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Supprimer")
                    }
                }

                if (!selectionMode && selectedEntries.isEmpty())
                    Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val selectingAll = selectedEntries.size < allEntries.size
                        selectedEntries = if (selectingAll) allEntries.toSet() else emptySet()
                        selectionMode = selectingAll
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    SvgIcon(
                        resId = if (selectedEntries.size < allEntries.size)
                            R.drawable.select_all_icon_vector
                        else
                            R.drawable.deselect_all_icon_vector,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                itemsIndexed(
                    filteredEntries,
                    key = { _, entry -> "${entry.type}-${entry.id}" }) { index, entry ->
                    EntryCardSwipeM3(
                        entry = entry,
                        shape = getItemShape(index, filteredEntries.size),
                        selectionMode = selectionMode,
                        isSelected = selectedEntries.contains(entry),
                        onClick = {
                            if (selectionMode) {
                                selectedEntries = toggleEntrySelection(selectedEntries, entry)
                                if (selectionMode && selectedEntries.isEmpty())
                                    selectionMode = false
                            } else {
                                selectionMode = true
                            }
                        },
                        onLongPress = {
                            selectionMode = true
                            selectedEntries = selectedEntries + entry
                        },
                        onDeleteFromDb = {
                            dataViewModel.deleteEntry(entry.id, entry.type.tableName)
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun EntryCardSwipeM3(
    entry: DbEntry,
    shape: Shape,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDeleteFromDb: () -> Unit,
    swipeThreshold: Dp = 100.dp
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val thresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }

    Surface(
        shape = shape,
        tonalElevation = 4.dp,
        color = if (selectionMode && isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        val newOffset = (offsetX.value + dragAmount).coerceIn(-300f, 300f)
                        scope.launch { offsetX.snapTo(newOffset) }
                    },
                    onDragEnd = {
                        if (abs(offsetX.value) > thresholdPx) {
                            onDeleteFromDb()
                        }
                        scope.launch {
                            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                        }
                    }
                )
            }
            .offset { androidx.compose.ui.unit.IntOffset(offsetX.value.toInt(), 0) }
            .combinedClickable(onClick = onClick, onLongClick = onLongPress)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            SvgIcon(
                resId = getIconForType(entry.type),
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(entry.title, fontWeight = FontWeight.Bold)
                Text(entry.subtitle, style = MaterialTheme.typography.bodySmall)
            }

            FlippableSelectionIcon(isSelected)
        }
    }
}

data class DbEntry(
    val id: Int,
    val type: AddableType,
    val title: String,
    val subtitle: String
)

fun mergeEntries(
    weights: List<WeightEntry>,
    hba1cs: List<HBA1CEntry>,
    appointments: List<Appointment>,
    treatments: List<Treatment>,
    diagnoses: List<DiagnosisDate>
): List<DbEntry> {
    return buildList {
        addAll(weights.map {
            DbEntry(
                it.id,
                AddableType.WEIGHT,
                "${it.weightKg} kg",
                it.date.toString()
            )
        })
        addAll(hba1cs.map {
            DbEntry(
                it.id,
                AddableType.HBA1C,
                "${it.value} %",
                it.date.toString()
            )
        })
        addAll(appointments.map {
            DbEntry(
                it.id,
                AddableType.APPOINTMENT,
                it.doctor,
                it.date.toString()
            )
        })
        addAll(treatments.map { DbEntry(it.id, AddableType.TREATMENT, it.name, "Traitement") })
        addAll(diagnoses.map {
            DbEntry(
                it.id,
                AddableType.DIAGNOSIS,
                it.diagnosis,
                it.date.toString()
            )
        })
    }
}

fun getIconForType(type: AddableType): Int {
    return when (type) {
        AddableType.WEIGHT -> R.drawable.weight_icon_vector
        AddableType.HBA1C -> R.drawable.hba1c_icon_vector
        AddableType.APPOINTMENT -> R.drawable.event_icon_vector
        AddableType.TREATMENT -> R.drawable.medication_icon_vector
        AddableType.DIAGNOSIS -> R.drawable.diagnosis_icon_vector
    }
}

fun toggleEntrySelection(current: Set<DbEntry>, entry: DbEntry): Set<DbEntry> {
    return if (current.contains(entry)) current - entry else current + entry
}

@Composable
fun FilterChips(
    types: List<AddableType>,
    selectedTypes: Set<AddableType>,
    onTypeToggle: (AddableType) -> Unit
) {
    val context = LocalContext.current

    val scrollState = rememberScrollState()
    val defaultBackground = MaterialTheme.colorScheme.surfaceVariant // léger contraste
    val selectedBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        types.forEach { type ->
            val isSelected = selectedTypes.contains(type)

            FilterChip(
                selected = isSelected,
                onClick = { onTypeToggle(type) },
                label = { Text(type.getDisplayName(context).uppercase()) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Outlined.Check, contentDescription = null) }
                } else null,
                border = null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = defaultBackground,
                    selectedContainerColor = selectedBackground,
                    labelColor = MaterialTheme.colorScheme.onSurface,
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
