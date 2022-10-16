package com.example.teachjr.data.model

import java.util.*
import kotlin.collections.ArrayList

data class LectureDocument constructor(
    val date: Date? = Date(),
    val stdAttended: List<String>? = ArrayList()
)