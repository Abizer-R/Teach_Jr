package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.model.User
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StudentRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {

    private val TAG = StudentRepository::class.java.simpleName
    private val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!


    suspend fun getUserDetails(): Response<StudentUser> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(Response.Success(snapshot.getValue(StudentUser::class.java)))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }

    suspend fun getCourseList(institute: String, branch: String, sem_sec: String): Response<List<RvStdCourseListItem>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.COURSE_COLLECTION)
                .child(institute)
                .child(branch)
                .child(sem_sec)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val courseList: MutableList<RvStdCourseListItem> = ArrayList()
                        for(courseSS in snapshot.children) {
                            val courseCode = courseSS.key.toString()
                            val courseName = courseSS.child(FirebasePaths.COURSE_NAME).value.toString()
                            val profName = courseSS.child(FirebasePaths.COURSE_PROF_NAME).value.toString()
                            courseList.add(RvStdCourseListItem(courseCode, courseName, profName))
                        }
//                        for(course in courseList) {
//                            Log.i(TAG, "StdTesting: courseItem - $course")
//                        }
                        continuation.resume(Response.Success(courseList))
                    }

                    override fun onCancelled(error: DatabaseError) {
//                        Log.i(TAG, "StdTesting: Error = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }
    
    suspend fun getLecDetails(sem_sec: String, courseCode: String): Response<Pair<Int, Int>> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ATTENDANCE_COLLECTION)
                .child(sem_sec)
                .child(courseCode)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        val lecCount = snapshot.child(FirebasePaths.LEC_COUNT).getValue(Int::class.java)
                        Log.i(TAG, "StudentTesting_Repo: LecCount - $lecCount")

                        // TODO: RETURN THE List<StdLecInfo>
                        var lecAttended: Int = 0
                        for(lecInfo in snapshot.child(FirebasePaths.LEC_LIST).children) {
                            val timestamp = lecInfo.child(FirebasePaths.TIMESTAMP).value.toString()
                            if(lecInfo.child(currentUser.uid).exists()) {
                                Log.i(TAG, "StudentTesting_Repo: $timestamp - present")
                                lecAttended++
                            } else {
                                Log.i(TAG, "StudentTesting_Repo: $timestamp - missed")
                            }
                        }
                        continuation.resume(Response.Success(Pair(lecCount!!, lecAttended)))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.i(TAG, "StudentTesting_Repo: getLecDetails_Error = ${error.message}")
                        continuation.resume(Response.Error(error.message, null))
                    }

                })
        }
    }

//    private suspend fun DatabaseReference.userDetails(): Response<User> = suspendCoroutine { continuation ->
//        val valueEventListener = object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if(snapshot.exists()) {
//                    continuation.resume(Response.Success(snapshot.getValue(User::class.java)))
//                } else {
//                    Log.i(TAG, "onDataChange: Something Went Wrong")
//                    continuation.resume(Response.Error("Something Went Wrong in repository.userDetails()", null))
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                continuation.resume(Response.Error(error.message, null))
//            }
//        }
//        // Subscribe to the callback
//        addListenerForSingleValueEvent(valueEventListener)
//    }



}