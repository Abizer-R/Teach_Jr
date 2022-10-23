package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.*
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

    private val TAG = ProfRepository::class.java.simpleName
    private val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!


    suspend fun getCourseList(): Response<List<RvProfCourseListItem>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .child(FirebasePaths.COURSE_PATHS_PROF)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val courseList: MutableList<RvProfCourseListItem> = ArrayList()
                        for(courseDetail in snapshot.children) {
                            val courseCode = courseDetail.key.toString()
                            val courseName = courseDetail.child(FirebasePaths.COURSE_NAME).value.toString()
                            for(sem_sec in courseDetail.child(FirebasePaths.PROF_SEM_SEC_LIST).children) {
                                courseList.add(RvProfCourseListItem(courseCode, courseName, sem_sec.key.toString()))
                            }

                        }
                        for(course in courseList) {
                            Log.i(TAG, "ProfessorTesting_ProRepo: course - $course")
                        }
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

    /**
     * It doesn't matter what we return here, all we need is confirmation
     */
    suspend fun createNewAtdRef(
        sem_sec: String, courseCode: String, timeInMilli: Long, lecCount: Int): Response<Boolean> {
        return suspendCoroutine { continuation ->

            val courseAtdPath = "/${FirebasePaths.ATTENDANCE_COLLECTION}/$sem_sec/$courseCode"
            val updates = hashMapOf<String, Any>(
                "$courseAtdPath/${FirebasePaths.LEC_COUNT}" to (lecCount+1),
                "$courseAtdPath/${FirebasePaths.LEC_LIST}/LEC_${lecCount+1}/timestamp" to timeInMilli
            )
            dbRef.reference.updateChildren(updates)
                .addOnSuccessListener {
                    continuation.resume(Response.Success(true))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }


}