package com.zawinski.timetable.model

sealed class ListItem {
    data class DateItem(val date: String) : com.zawinski.timetable.model.ListItem()
    data class ListItem(val item: ItemData) : com.zawinski.timetable.model.ListItem()
    object LoadingItem : com.zawinski.timetable.model.ListItem()
    object NoData : com.zawinski.timetable.model.ListItem()
}

data class ItemData(
    val title: String,
    val body: String
)

fun createDummyListItems() = listOf<ListItem>(
    ListItem.DateItem("Mon May 15"),
    ListItem.LoadingItem,
    ListItem.DateItem("Tues May 16"),
    ListItem.LoadingItem,
    ListItem.DateItem("Wed May 17"),
    ListItem.LoadingItem,
    ListItem.DateItem("Thurs May 18"),
    ListItem.LoadingItem,
    ListItem.DateItem("Friday May 19"),
    ListItem.LoadingItem
)
