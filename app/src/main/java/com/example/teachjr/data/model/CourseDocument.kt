package com.example.teachjr.data.model

import com.google.firebase.database.Exclude

data class CourseDocument constructor(
    @Exclude
    var courseId: String? = null,
    val courseCode: String? = "",
    val courseName: String? = "",
    val profId: String? = "",
    val profName: String? = "",
    val enrolDocId: String? = "",
    val lecDocId: String? = ""
)