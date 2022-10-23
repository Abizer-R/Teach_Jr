package com.example.teachjr.data.model

data class StdAttendanceDetails(
    val totalLecCount: Int = 0,
    val attendedLecCount: Int = 0,
    val missedLecCount: Int = 0,
    val lecList: List<RvStdLecListItem> = ArrayList()
)
