package com.example.teachjr.data.model

data class LecturesDocument constructor(
    val courseId: String? = "",
    val lecCount: Int? = 0,
    val lecList: List<Lecture>? = ArrayList()
)
