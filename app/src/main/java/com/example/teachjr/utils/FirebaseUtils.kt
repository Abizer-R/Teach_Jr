package com.example.teachjr.utils

object FirebaseConstants {

    const val TYPE_STUDENT: String = "student"
    const val TYPE_PROFESSOR: String = "professor"
    const val userType: String = "userType"

}

object FirebasePaths {

    const val ATTENDANCE_COLLECTION = "Attendance_Collection"
    const val ATD_LEC_COUNT = "lecCount"
    const val ATD_TIMESTAMP = "epoch_timestamp"

    const val COURSE_COLLECTION = "Course_Collection"
    const val COURSE_NAME = "name"
    const val COURSE_PROF_NAME = "profName"

    const val ENROLLMENT_COLLECTION = "Enrollment_Collection"
    const val ENRL_STUDENT_COUNT = "stdCount"
    const val ENRL_STUDENT_LIST = "student_list"

    const val USER_COLLECTION = "User_Collection"


    const val USER_INFO = "User Info"
    const val USER_EMAIL = "email"
    const val USER_ID = "id"
    const val USER_NAME = "name"
    const val USER_ENROLLMENT_STUDENT = "enrollment"
    const val COURSE_LIST = "courseList"

    const val COURSE_ID = "courseId"


    const val  COURSE_CODE = "courseCode"
    const val  PROF_ID = "profId"
    const val  PROF_NAME = "profName"
    const val  ENROLLMENT_DOC_ID = "enrolDocId"
    const val  LECTURE_DOC_ID = "lecDocId"

    const val LECTURE_COLLECTION = "Lectures Collection"
    const val ENROLLMENT_REQ_COUNT = "reqCount"
    const val ENROLLMENT_REQ_LIST = "reqList"
}