package com.zawinski.timetable

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zawinski.timetable.model.ItemData
import com.zawinski.timetable.model.ListItem
import com.zawinski.timetable.model.ScheduleUiHeader
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableScreen() {
    val viewModel = TimetableViewModel()
    val items by viewModel.items.collectAsState()
    val myEvent by viewModel.myEvent.collectAsState(TimetableEvent.Idle)
    val headerItems by viewModel.headerItems.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    when (val event = myEvent) {
        is TimetableEvent.FastScrollTo -> {
            LaunchedEffect(event) {
                scope.launch {
                    listState.animateScrollToItem(event.index)
                }
            }
        }
        TimetableEvent.Idle -> Unit
    }
    Scaffold() { paddingValues ->
        if (listState.isScrollInProgress) {
            Log.d("TimetableScreen", "Is Scroll in progress: ${viewModel.isFastScrolling}")
            val firstVisible = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

            viewModel.fetchBetweenTwoVisibleItems(firstVisible, lastVisible + 1)
        }
        Column {
            DateHeader(
                selectedId = viewModel.currentTrack.value,
                i = headerItems,
                onClick = {
                    viewModel.isFastScrolling = true
                    viewModel.fastScrollToId(it)
                }
            )
            LazyColumn(
                modifier = Modifier.padding(paddingValues),
                state = listState
            ) {
                for (index in items.indices) {
                    when (val item = items[index]) {
                        is ListItem.DateItem -> stickyHeader {
                            Text(
                                text = item.date,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.LightGray)
                            )
                        }
                        is ListItem.ListItem -> item {
                            ListDataItem(data = item.item)
                        }
                        ListItem.LoadingItem -> item {
                            Box(
                                modifier = Modifier.height(1000.dp)
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        ListItem.NoData -> item {
                            Card {
                                Text(text = "No Data")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(
    selectedId: Int,
    i: List<ScheduleUiHeader>,
    onClick: (Int) -> Unit
) {
    LazyRow {
        items(i) {
            Card(
                backgroundColor = if (selectedId == it.id) Color.Blue else Color.White,
                modifier = Modifier.clickable {
                    onClick(it.id)
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = it.name, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ListDataItem(data: ItemData) {
    Card(
        modifier = Modifier.height(156.dp).fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = data.title)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = data.body)
        }
    }
}
