package com.example.teachjr.data.model

data class CourseDocument constructor(
    val courseCode: String? = "",
    val courseName: String? = "",
    val profId: String? = "",
    val profName: String? = "",
    val enrollmentInfo: EnrollmentInfo? = EnrollmentInfo(),
    val lectureCollection: LectureCollection? = LectureCollection()
)