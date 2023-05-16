package com.zawinski.timetable

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zawinski.timetable.model.ItemData
import com.zawinski.timetable.model.ListItem
import com.zawinski.timetable.model.ScheduleUiHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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
    var isFastScrolling = false
    private val _myEvent = MutableStateFlow<TimetableEvent>(TimetableEvent.Idle)
    val myEvent: StateFlow<TimetableEvent> = _myEvent

    init {
        brewData()
        fetchFirstDate()
    }

    fun emitEvent(event: TimetableEvent) {
        viewModelScope.launch {
            _myEvent.tryEmit(event)
        }
    }

    private fun fetchFirstDate() = viewModelScope.launch {
        val temp = _items.value.toMutableList()
        temp.removeAt(1)
        temp.addAll(1, createDummyDate())
        delay(1000)
        currentTrack.value = 0
        _items.value = temp
        Log.d("FastScroll", "Initial fetchFirstDate: ${_items.value.size}")
    }

    fun fastScrollToId(id: Int) = viewModelScope.launch {
        currentTrack.value = id
        val dateById = _items.value.firstOrNull { it is ListItem.DateItem && it.id == id }
        Log.d("FastScroll", "fastScrollToId: $dateById")
        val index = _items.value.indexOf(dateById)
        Log.d("FastScroll", "Index: $index")
        val itemAtIndex = _items.value[index + 1]
        Log.d("FastScroll", "Item at Index: $itemAtIndex")
        Log.d("FastScroll", "Before Not loading: ${_items.value[index]}")
        if (itemAtIndex is ListItem.LoadingItem) {
            emitEvent(TimetableEvent.FastScrollTo(index))
            val temp = _items.value.toMutableList()
            temp.removeAt(index + 1)
            val dummy = createDummyDate()
            temp.addAll(index + 1, dummy)
            _items.value = temp
            isFastScrolling = false
        } else {
            Log.d("FastScroll", "Not Loading Item index: $index")
            Log.d("FastScroll", "Not Loading Item: ${_items.value[index]}")
            Log.d("FastScroll", "Not Loading Items Size: ${_items.value.size}")
            isFastScrolling = true
            emitEvent(TimetableEvent.FastScrollTo(index))
            delay(5000)
            isFastScrolling = false
        }
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

    fun takeLastDateHeader(end: Int) {
    }

    fun fetchBetweenTwoVisibleItems(start: Int, end: Int) = viewModelScope.launch {
        if (isLoading || isFastScrolling) {
            return@launch
        }
        Log.d("FastScroll", "Called: Start $start and End $end and Total ${_items.value.size}")
        val dateItemBetween = getDateItemBetween(start, end)
        if (start == 0) {
            val lastIndex = _items.value.indexOfFirst { (it is ListItem.DateItem) }
            currentTrack.value = (_items.value[lastIndex] as ListItem.DateItem).id
        } else {
            currentTrack.value = dateItemBetween
        }

        val currentItems = _items.value
        for (i in start until end) {
            val listItem = currentItems[i]
            if (listItem is ListItem.LoadingItem) {
                if (i != 0) {
                    val current = currentItems[i - 1]
                    val isActuallyDate = current is ListItem.DateItem
                    if (isActuallyDate) {
                        isLoading = true
                        val temp = currentItems.toMutableList()
                        temp.removeAt(i)
//                        currentTrack.value = (current as ListItem.DateItem).id
//                        temp.add(i, ListItem.NoData)
                        temp.addAll(i, createDummyDate())
                        delay(1000)
                        _items.value = temp.toList()
                        isLoading = false
                    }
                }
            }
        }
    }

    private fun getDateItemBetween(start: Int, end: Int): Int {
        for (i in end - 1 downTo start) {
            val listItem = _items.value[i]
            if (listItem is ListItem.DateItem) {
                return listItem.id
            }
        }
        return 0
    }

    private fun createDummyDate() = listOf<ListItem.ListItem>(
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body"))
    )
}

sealed class TimetableEvent {
    object Idle : TimetableEvent()
    data class FastScrollTo(val index: Int) : TimetableEvent()
}
