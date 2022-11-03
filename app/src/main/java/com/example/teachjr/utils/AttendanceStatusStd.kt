package com.example.teachjr.utils

sealed class AttendanceStatusStd(val timestamp: String? = null, val errorMessage: String? = null)
{
//    class InitiatingDiscovery: AttendanceStatusStd()
    class DiscoveringTimestamp : AttendanceStatusStd()
    class TimestampDiscovered (timestamp: String) : AttendanceStatusStd(timestamp = timestamp)
    class AttendanceMarked () : AttendanceStatusStd()
    class BroadcastComplete () : AttendanceStatusStd()
    class Error (errorMessage: String) : AttendanceStatusStd(errorMessage = errorMessage)
}
