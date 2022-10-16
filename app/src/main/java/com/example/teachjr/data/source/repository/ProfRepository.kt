package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.CourseDocument
import com.example.teachjr.data.model.EnrollmentInfo
import com.example.teachjr.data.model.LectureCollection
import com.example.teachjr.data.model.User
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ProfRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {
    private val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!

    suspend fun getUserDetails(): Response<User> {
        Log.i("TAG", "GET USER DETAILS-Repo: Calling userDetails()")
        return  dbRef.getReference(FirebasePaths.USER_COLLECTION)
            .child(currentUser.uid)
            .userDetails()
    }

    private suspend fun DatabaseReference.userDetails(): Response<User> = suspendCoroutine { continuation ->
        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(Response.Success(snapshot.getValue(User::class.java)))
            }

            override fun onCancelled(error: DatabaseError) {
                 continuation.resume(Response.Error(error.message, null))
            }
        }
        // Subscribe to the callback
        addListenerForSingleValueEvent(valueEventListener)
    }

    suspend fun createCourse(
        courseCode: String, courseName: String, profName: String): Response<String> {
        try {

            val newCourse: CourseDocument = CourseDocument(
                courseCode = courseCode,
                courseName = courseName,
                profId = currentUser.uid,
                profName = profName,
                enrollmentInfo = EnrollmentInfo(),
                lectureCollection = LectureCollection()
            )

            val courseRef = dbRef.getReference(FirebasePaths.COURSE_COLLECTION).push()
            courseRef.setValue(newCourse).await()

            // Not exposing the direct ID in order to enhance security
            return Response.Success("$courseCode/${courseRef.key}")
        } catch (e: Exception) {
            e.printStackTrace()
            return Response.Error(e.message.toString(), null)
        }
    }

//    suspend fun getCourseDocs(): List<CourseDocument> {
//        // TODO: This function's code is on hold
//        val courseIDList = getCourseIDList()
//
//    }

    suspend fun getCourseIDList(): Response<List<String>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(FirebasePaths.COURSE_LIST)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Log.i("Getting Course List", "onDataChange: snapshot: $snapshot")

                        val courseIDList: MutableList<String> = mutableListOf()
                        for(courseID in snapshot.children) {
                            courseIDList.add(courseID.value.toString())
                        }
                        continuation.resume(Response.Success(courseIDList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }
}