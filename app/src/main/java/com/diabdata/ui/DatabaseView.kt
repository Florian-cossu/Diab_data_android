package com.diabdata.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.AddableType
import com.diabdata.ui.components.FlippableSelectionIcon
import com.diabdata.utils.SvgIcon
import com.diabdata.utils.getItemShape
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun DatabaseEditionView(
    dataViewModel: DataViewModel
) {
    val context = LocalContext.current
    var selectedTypes by remember { mutableStateOf(setOf<AddableType>()) }
    var selectionMode by remember { mutableStateOf(false) }
    var selectedEntries by remember { mutableStateOf(setOf<DataViewModel.MixedDbEntry>()) }

    val mixedEntries by dataViewModel.allMixedEntries.collectAsState(emptyList())
    val filteredEntries = if (selectedTypes.isEmpty()) mixedEntries
    else mixedEntries.filter { it.addableType in selectedTypes }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
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

            // BOUTONS DE SUPPRESSION / SELECT ALL
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            selectedEntries.forEach { dataViewModel.deleteEntry(it) }
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
                            SvgIcon(resId = R.drawable.delete_icon_vector, color = textColor)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(context.getString(R.string.delete_button_text))
                    }
                }

                if (!selectionMode && selectedEntries.isEmpty()) Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val selectingAll = selectedEntries.size < filteredEntries.size
                        selectedEntries = if (selectingAll) filteredEntries.toSet() else emptySet()
                        selectionMode = selectingAll
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    SvgIcon(
                        resId = if (selectedEntries.size < filteredEntries.size)
                            R.drawable.select_all_icon_vector
                        else R.drawable.deselect_all_icon_vector,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // LISTE DES ENTRIES
            LazyColumn {
                items(
                    items = filteredEntries,
                    key = { entry -> "${entry.addableType}-${entry.id}" }
                ) { entry ->
                    EntryCardSwipeM3(
                        entry = entry,
                        shape = getItemShape(filteredEntries.indexOf(entry), filteredEntries.size),
                        selectionMode = selectionMode,
                        isSelected = selectedEntries.contains(entry),
                        onClick = {
                            if (selectionMode) {
                                selectedEntries = toggleEntrySelection(selectedEntries, entry)
                                if (selectedEntries.isEmpty()) selectionMode = false
                            }
                        },
                        onLongPress = {
                            selectionMode = true
                            selectedEntries = selectedEntries + entry
                        },
                        onDeleteFromDb = { dataViewModel.deleteEntry(entry) },
                        onArchive = {}
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun EntryCardSwipeM3(
    entry: DataViewModel.MixedDbEntry,
    modifier: Modifier = Modifier,
    shape: Shape,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onDeleteFromDb: () -> Unit,
    onArchive: () -> Unit,
    swipeThreshold: Dp = 100.dp
) {
    DateTimeFormatter.ofPattern("dd MMM yyyy")
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val alphaAnim = remember { Animatable(1f) }
    val thresholdPx = with(LocalDensity.current) { swipeThreshold.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // BACKGROUND SWIPE
        Box(modifier = Modifier.matchParentSize()) {
            val progress = (abs(offsetX.value) / thresholdPx).coerceIn(0f, 1f)
            if (offsetX.value > 0) {
                Surface(
                    shape = shape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f + 0.8f * progress),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart
                    ) {
                        SvgIcon(
                            resId = R.drawable.delete_icon_vector,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(progress)
                                .padding(start = 16.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            } else if (offsetX.value < 0) {
                Surface(
                    shape = shape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f + 0.8f * progress),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd
                    ) {
                        SvgIcon(
                            resId = R.drawable.inbox_icon_vector,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(progress)
                                .padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }

        Surface(
            shape = shape,
            tonalElevation = 4.dp,
            color = if (selectionMode && isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offsetX.value
                    alpha = alphaAnim.value
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(onHorizontalDrag = { _, dragAmount ->
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                    }, onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > thresholdPx -> {
                                    offsetX.animateTo(1000f, tween(200)); alphaAnim.animateTo(
                                        0f,
                                        tween(200)
                                    ); onDeleteFromDb()
                                }

                                offsetX.value < -thresholdPx -> {
                                    offsetX.animateTo(-1000f, tween(200)); alphaAnim.animateTo(
                                        0f,
                                        tween(200)
                                    ); onArchive()
                                }

                                else -> offsetX.animateTo(
                                    0f,
                                    spring(stiffness = Spring.StiffnessMedium)
                                )
                            }
                        }
                    })
                }
                .combinedClickable(onClick = onClick, onLongClick = onLongPress)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                SvgIcon(
                    resId = entry.icon,
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    EntryContent(entry)
                }
                FlippableSelectionIcon(isSelected)
            }

        }
    }
}

@Composable
private fun EntryContent(entry: DataViewModel.MixedDbEntry) {
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    when (entry) {
        is DataViewModel.MixedDbEntry.AppointmentEntry -> {
            Text(entry.doctor, fontWeight = FontWeight.Bold)
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(entry.type.displayNameRes),
                style = MaterialTheme.typography.bodySmall
            )
            entry.notes?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        is DataViewModel.MixedDbEntry.DiagnosisEntry -> {
            Text(entry.diagnosis, fontWeight = FontWeight.Bold)
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
        }

        is DataViewModel.MixedDbEntry.Hba1cEntry -> {
            Text("${entry.value} %", fontWeight = FontWeight.Bold)
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
        }

        is DataViewModel.MixedDbEntry.TreatmentEntry -> {
            Text(entry.name, fontWeight = FontWeight.Bold)
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
            Text(
                stringResource(entry.treatmentType.displayNameRes),
                style = MaterialTheme.typography.bodySmall
            )
        }

        is DataViewModel.MixedDbEntry.WeightEntry -> {
            Text("${entry.value} kg", fontWeight = FontWeight.Bold)
            Text(entry.date.format(formatter), style = MaterialTheme.typography.bodySmall)
        }
    }
}

fun toggleEntrySelection(
    current: Set<DataViewModel.MixedDbEntry>,
    entry: DataViewModel.MixedDbEntry
): Set<DataViewModel.MixedDbEntry> {
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
    val defaultBackground = MaterialTheme.colorScheme.surfaceVariant
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