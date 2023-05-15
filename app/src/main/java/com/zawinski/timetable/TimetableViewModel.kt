package com.zawinski.timetable

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zawinski.timetable.model.ItemData
import com.zawinski.timetable.model.ListItem
import com.zawinski.timetable.model.ScheduleUiHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TimetableViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ListItem>>(emptyList())
    val items: StateFlow<List<ListItem>> = _items
    private val _headerItems = MutableStateFlow<List<ScheduleUiHeader>>(emptyList())
    val headerItems: StateFlow<List<ScheduleUiHeader>> = _headerItems
    var currentTrack = mutableStateOf<Int>(0)
    var isLoading = false

    init {
        brewData()
        fetchFirstDate()
    }

    private fun fetchFirstDate() = viewModelScope.launch {
        val temp = _items.value.toMutableList()
        temp.removeAt(1)
        temp.addAll(1, createDummyDate())
        delay(1000)
        currentTrack.value = 0
        _items.value = temp
    }

    private fun brewData() {
        val days = getNextSevenDays().mapIndexed { index, s ->
            ScheduleUiHeader(
                day = s.split(" ").first().toInt(),
                name = if (index == 0) "Today" else s.split(" ")[1],
                holder = s.split(" ")[2],
                id = index
            )
        }
        _headerItems.value = days
        val listItems = mutableListOf<ListItem>()
        days.forEach {
            listItems.add(ListItem.DateItem(date = "${it.name} ${it.day}", id = it.id))
            listItems.add(ListItem.LoadingItem)
        }
        _items.value = listItems
    }

    private fun getNextSevenDays(): List<String> {
        val dateFormat = SimpleDateFormat("dd EEE yyyy-MM-dd", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        val dates = mutableListOf<String>()
        for (i in 1..7) {
            dates.add(dateFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return dates
    }

    fun fetchBetweenTwoVisibleItems(start: Int, end: Int) = viewModelScope.launch {
        if (isLoading) {
            return@launch
        }
        val currentItems = _items.value
        for (i in start until end) {
            val listItem = currentItems[i]
            if (listItem is ListItem.LoadingItem) {
                if (i != 0) {
                    val isActuallyDate = currentItems[i - 1] is ListItem.DateItem
                    if (isActuallyDate) {
                        currentTrack.value = (currentItems[i - 1] as ListItem.DateItem).id
                        isLoading = true
                        val temp = currentItems.toMutableList()
                        temp.removeAt(i)
//                        temp.add(i, ListItem.NoData)
                        temp.addAll(i, createDummyDate())
                        delay(1000)
                        _items.value = temp
                        isLoading = false
                    }
                }
            }
        }
    }

    private fun createDummyDate() = listOf<ListItem.ListItem>(
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body"))
    )
}
