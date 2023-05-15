package com.zawinski.timetable

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zawinski.timetable.model.ItemData
import com.zawinski.timetable.model.ListItem
import com.zawinski.timetable.model.createDummyListItems
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimetableViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ListItem>>(emptyList())
    val items: StateFlow<List<ListItem>> = _items
    var isLoading = false

    init {
        _items.value = createDummyListItems()
        fetchFirstDate()
    }

    private fun fetchFirstDate() = viewModelScope.launch {
        val temp = _items.value.toMutableList()
        temp.removeAt(1)
        temp.addAll(1, createDummyDate())
        delay(1000)
        _items.value = temp
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
                        isLoading = true
                        val temp = currentItems.toMutableList()
                        temp.removeAt(i)
                        temp.add(i, ListItem.NoData)
//                        temp.addAll(i, createDummyDate())
                        delay(1000)
                        _items.value = temp
                        isLoading = false
                    }
                }
            }
        }
    }

    fun checkDateItemOrNot(index: Int) {
        val isDateItem = _items.value[index] is ListItem.DateItem
        if (isDateItem) {
            fetchForDate(_items.value[index])
        }
    }

    fun fetchForDate(item: ListItem) = viewModelScope.launch {
        if (isLoading) {
            return@launch
        }
        val index = _items.value.indexOf(item)
        val hasReachedLimit = index >= _items.value.size - 1
        if (hasReachedLimit) {
            return@launch
        }
        val hasAlreadyFetched = _items.value[index + 1] != ListItem.LoadingItem
        if (!hasAlreadyFetched) {
            isLoading = true
            val temp = _items.value.toMutableList()
            temp.removeAt(index + 1)
            temp.add(index + 1, ListItem.NoData)
//            temp.addAll(index + 1, createDummyDate())
            delay(1000)
            _items.value = temp
            isLoading = false
        }
    }

    private fun checkDownwardShouldFetch(index: Int, input: List<ListItem>): Pair<Boolean, Int> {
        for (i in index downTo 0) {
            val ok = input[i] is ListItem.DateItem && input[index + 1] == ListItem.LoadingItem
            if (ok) {
                Log.d("TimetableVM", "checkDownwardShouldFetch: $i")
            }
            return ok to i
        }
        return false to 0
    }

    private fun createDummyDate() = listOf<ListItem.ListItem>(
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body")),
        ListItem.ListItem(ItemData("Item ${(0..1000).random()}", "Item Body"))
    )
}
