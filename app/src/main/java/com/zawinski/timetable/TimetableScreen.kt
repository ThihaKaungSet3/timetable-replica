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
            val index = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            viewModel.checkDateItemOrNot(index)
            Log.d("TimetableScreen", "Scrolling: $index")
        }
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            state = listState
        ) {
            for (index in state.indices) {
                val item = state[index]

                when (item) {
                    is ListItem.DateItem -> stickyHeader {
                        Text(
                            text = item.date.toString(),
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
