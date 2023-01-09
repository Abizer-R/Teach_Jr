package com.example.teachjr.data.model

import com.example.teachjr.utils.sealedClasses.AttendanceStatusRvItem

data class RvProfMarkAtdListItem (
    val enrollment: String = "",
    val atdStatus: AttendanceStatusRvItem = AttendanceStatusRvItem.Absent()
)