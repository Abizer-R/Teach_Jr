package com.example.teachjr.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RvCourseListItem(
    val courseCode: String = "",
    val courseName: String = "",
    val sem_sec: String? = null // Only for professors
): Parcelable
