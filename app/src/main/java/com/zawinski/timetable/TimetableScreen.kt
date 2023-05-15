package com.zawinski.timetable

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zawinski.timetable.model.ItemData
import com.zawinski.timetable.model.ListItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimetableScreen() {
    val viewModel: TimetableViewModel = TimetableViewModel()
    val state by viewModel.items.collectAsState()
    val listState = rememberLazyListState()
    Scaffold() { paddingValues ->
        if (listState.isScrollInProgress) {
            val firstVisible = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            Log.d("TimetableScreen", "First Visible: $firstVisible and Last Visible: $lastVisible")
            viewModel.fetchBetweenTwoVisibleItems(firstVisible, lastVisible + 1)
//            viewModel.checkDateItemOrNot(firstVisible)
        }
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            state = listState
        ) {
            for (index in state.indices) {
                when (val item = state[index]) {
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

@Composable
private fun ListDataItem(data: ItemData) {
    Card {
        Column() {
            Text(text = data.title)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = data.body)
        }
    }
}
