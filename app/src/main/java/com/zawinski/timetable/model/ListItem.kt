package com.zawinski.timetable.model

sealed class ListItem {
    data class DateItem(val date: String, val id: Int) : com.zawinski.timetable.model.ListItem()
    data class ListItem(val item: ItemData) : com.zawinski.timetable.model.ListItem()
    object LoadingItem : com.zawinski.timetable.model.ListItem()
    object NoData : com.zawinski.timetable.model.ListItem()
}


data class ScheduleUiHeader(
    val id: Int,
    val day: Int,
    val name: String,
    val holder: String
)

data class ItemData(
    val title: String,
    val body: String
)
