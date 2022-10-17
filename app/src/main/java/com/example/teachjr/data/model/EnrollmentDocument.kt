package com.example.teachjr.data.model

data class EnrollmentDocument constructor(
        val courseId: String? = "",
        val reqCount: Int? = 0,
        val reqStdIDs: List<String>? = ArrayList(),
        val stdCount: Int? = 0,
        val StdList: List<String>? = ArrayList()
)