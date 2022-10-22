package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.*
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.log

class ProfRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {

    private val TAG = ProfRepository::class.java.simpleName
    private val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!

    suspend fun getUserDetails(): Response<User> {
        Log.i("TAG", "GET USER DETAILS-Repo: Calling userDetails()")
        return  dbRef.getReference(FirebasePaths.USER_COLLECTION)
            .child(FirebasePaths.USER_INFO)
            .child(currentUser.uid)
            .userDetails()
    }

    private suspend fun DatabaseReference.userDetails(): Response<User> = suspendCoroutine { continuation ->
        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    continuation.resume(Response.Success(snapshot.getValue(User::class.java)))
                } else {
                    Log.i(TAG, "onDataChange: Something Went Wrong")
                    continuation.resume(Response.Error("Something Went Wrong in repository.userDetails()", null))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                 continuation.resume(Response.Error(error.message, null))
            }
        }
        // Subscribe to the callback
        addListenerForSingleValueEvent(valueEventListener)
    }


    suspend fun getCourseList(): Response<List<RvCourseListItem>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .child(FirebasePaths.COURSE_PATHS_PROF)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val courseList: MutableList<RvCourseListItem> = ArrayList()

                        for(courseDetail in snapshot.children) {
                            val courseCode = courseDetail.key.toString()
                            val courseName = courseDetail.child(FirebasePaths.NAME).value.toString()
                            for(sem_sec in courseDetail.child(FirebasePaths.PROF_SEM_SEC_LIST).children) {
                                courseList.add(RvCourseListItem(courseCode, courseName, sem_sec.key.toString()))
                            }

                        }
//                        for(course in courseList) {
//                            Log.i(TAG, "ProfTesting: courseItem = $course")
//                        }
                        continuation.resume(Response.Success(courseList))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }


    suspend fun getLectureCount(sem_sec: String, courseCode: String): Response<Int> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child(sem_sec)
                .child(courseCode)
                .child(FirebasePaths.LEC_COUNT)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(Response.Success(snapshot.getValue(Int::class.java)))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }
                })
        }
    }

    // TODO: Change SingleValueEventListener to onDataChangeListener
    private suspend fun DatabaseReference.getCourseDocument(): Response<CourseDocument> = suspendCoroutine { continuation ->
        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val courseDocument = snapshot.getValue(CourseDocument::class.java)
                    courseDocument!!.courseId = snapshot.key
                    continuation.resume(Response.Success(courseDocument))
                } else {
                    Log.i(TAG, "onDataChange-getCourseDocument: Something Went Wrong")
                    continuation.resume(Response.Error("Something Went Wrong in repository.getCourseDocument()", null))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Response.Error(error.message, null))
            }
        }
        // Subscribe to the callback
        addListenerForSingleValueEvent(valueEventListener)
    }

    suspend fun getLectureDoc(courseId: String): Response<LecturesDocument> {
        val lecId = dbRef.getReference(FirebasePaths.COURSE_COLLECTION)
            .child(courseId)
            .child(FirebasePaths.LECTURE_DOC_ID)
            .get().await()

        Log.i(TAG, "getLectureDoc: LecId = ${lecId.value.toString()}")

        return dbRef.getReference(FirebasePaths.LECTURE_COLLECTION)
            .child(lecId.value.toString())
            .getLectureDocument()
    }

    private suspend fun DatabaseReference.getLectureDocument(): Response<LecturesDocument> = suspendCoroutine { continuation ->
        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    continuation.resume(Response.Success(snapshot.getValue(LecturesDocument::class.java)))
                } else {
                    Log.i(TAG, "onDataChange-getLectureDocument: Something Went Wrong")
                    continuation.resume(Response.Error("Something Went Wrong in repository.getLectureDocument()", null))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Response.Error(error.message, null))
            }
        }
        // Subscribe to the callback
        addListenerForSingleValueEvent(valueEventListener)
    }
}