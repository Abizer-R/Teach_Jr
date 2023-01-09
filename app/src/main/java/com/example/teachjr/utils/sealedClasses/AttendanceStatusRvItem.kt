package com.example.teachjr.utils.sealedClasses

sealed class AttendanceStatusRvItem {
    class Absent: AttendanceStatusRvItem()
    class Present_Wifi_SD: AttendanceStatusRvItem()
    class Present_Prof_Manual: AttendanceStatusRvItem()
}
