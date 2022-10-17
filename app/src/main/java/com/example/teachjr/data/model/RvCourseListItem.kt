package com.example.teachjr.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RvCourseListItem(
    val courseId: String = "",
    val courseCode: String = "",
    val courseName: String = ""
): Parcelable
