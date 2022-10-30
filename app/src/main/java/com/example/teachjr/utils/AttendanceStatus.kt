package com.example.teachjr.utils

sealed class AttendanceStatus(val timestamp: String? = null, val errorMessage: String? = null)
{
    class FetchingTimestamp : AttendanceStatus()
    class Initiated (timestamp: String) : AttendanceStatus(timestamp = timestamp)
    class Ended : AttendanceStatus()
    class Error (errorMessage: String) : AttendanceStatus(errorMessage = errorMessage)
}
