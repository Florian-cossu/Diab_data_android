package com.diabdata.utils

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@SuppressLint("FrequentlyChangingValue")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    range: IntRange,
    selected: Int,
    onSelectedChange: (Int) -> Unit,
    visibleCount: Int = 3,
    itemHeight: Dp = 40.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (selected - range.first)  // positionner sélection initiale
    )

    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Box(
        modifier = modifier
            .border(
                color = MaterialTheme.colorScheme.primary,
                width = 1.dp,
                shape = MaterialTheme.shapes.medium
            ),
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = (visibleCount / 2) * itemHeight),
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.typography.bodyLarge.fontSize.value.dp * 6f)
        ) {
            items(range.toList()) { value ->
                val index = value - range.first
                val isSelected = index == listState.firstVisibleItemIndex + (visibleCount / 2)
                Text(
                    text = value.toString(),
                    style = textStyle.copy(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
        }

        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { firstIndex ->
                    val newSelected = range.first + firstIndex + (visibleCount / 2)
                    if (newSelected in range) {
                        onSelectedChange(newSelected)
                    }
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NumberPickerPreview() {
    var selected by remember { mutableIntStateOf(5) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Valeur sélectionnée : $selected")
            Spacer(modifier = Modifier.height(16.dp))
            NumberPicker(
                range = 0..20,
                selected = selected,
                onSelectedChange = { selected = it },
                visibleCount = 5
            )
        }
    }
}