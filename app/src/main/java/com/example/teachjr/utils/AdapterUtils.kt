package com.example.teachjr.utils

object AdapterUtils {

    fun getSection(sem_sec: String): String {
        val secIdx = sem_sec.indexOf("_", 0)
        return sem_sec.substring(secIdx + 1)
    }

//    fun getSemInFormat(sem_sec: String, addedString: String): String {
//        val secIdx = sem_sec.indexOf("_", 0)
//        return sem_sec.substring(0, secIdx) + addedString
//    }
}