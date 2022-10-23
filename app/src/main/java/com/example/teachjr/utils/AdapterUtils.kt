package com.example.teachjr.utils

import java.text.SimpleDateFormat
import java.util.*

object AdapterUtils {

    fun getSection(sem_sec: String): String {
        val secIdx = sem_sec.indexOf("_", 0)
        return sem_sec.substring(secIdx + 1)
    }

//    fun getSemInFormat(sem_sec: String, addedString: String): String {
//        val secIdx = sem_sec.indexOf("_", 0)
//        return sem_sec.substring(0, secIdx) + addedString
//    }

    fun getFormattedDate(timestamp: String): String {
        try {
            val sdf = SimpleDateFormat("EEE, MMM dd, yyyy")
            val netDate = Date(timestamp.toLong() * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
}