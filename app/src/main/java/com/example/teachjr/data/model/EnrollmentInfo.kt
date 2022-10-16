package com.example.teachjr.data.model

data class EnrollmentInfo constructor(
        val reqCount: Int? = 0,
        val reqStdIDs: List<String>? = ArrayList(),
        val enrolledStdCount: Int? = 0,
        val enrolledStdIDs: List<String>? = ArrayList()
)