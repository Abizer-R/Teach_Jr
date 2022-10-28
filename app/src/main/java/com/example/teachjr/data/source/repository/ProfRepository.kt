package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.*
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
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

//    suspend fun getStdList(sem_sec: String): Response<List<String>> {
//        return suspendCoroutine { continuation ->
//            dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION)
//                .child(sem_sec)
//                .child(FirebasePaths.ENRL_STUDENT_LIST)
//                .get()
//                .addOnSuccessListener {
//                    val stdList: MutableList<String> = ArrayList()
//                    for(students in it.children) {
//                        stdList.add(students.key.toString())
//                    }
//                }
//                .addOnFailureListener {
//                    continuation.resume(Response.Error(it.message.toString(), null))
//                }
//        }
//    }

    /**
     * It doesn't matter what we return here, all we need is confirmation
     */
    suspend fun createNewAtdRef(
        sem_sec: String, courseCode: String, timestamp: String, lecCount: Int): Response<Boolean> {
        return suspendCoroutine { continuation ->
            Log.i(TAG, "createNewAtdRef: TIMESTAMP = $timestamp")

            val courseAtdPath = "/${FirebasePaths.ATTENDANCE_COLLECTION}/$sem_sec/$courseCode"
            val updates = hashMapOf<String, Any>(
                "$courseAtdPath/${FirebasePaths.LEC_COUNT}" to (lecCount+1),
                "$courseAtdPath/${FirebasePaths.LEC_LIST}/$timestamp/${FirebasePaths.ATD_IS_CONTINUING}" to true
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

    suspend fun isAtdContinuing(lecPath: String): Response<Boolean> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(lecPath)
                .child(FirebasePaths.ATD_IS_CONTINUING)
                .get()
                .addOnSuccessListener {
                    continuation.resume(Response.Success(it.getValue(Boolean::class.java)))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }

    /**
     * TODO: User flow to return the stdId as they get added to the lecList
     */
    fun observeAttendance(lecPath: String): Flow<String> =
        dbRef.getReference(lecPath)
            .observeChildEvent()
            .catch { Log.i(TAG, "ProfessorTesting_ProRepo - observeAttendance: ERROR = ${it.message}") }

    fun DatabaseReference.observeChildEvent(): Flow<String> = callbackFlow {
            val childEventListener = object : ChildEventListener {
                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildAdded: snapshot - $snapshot, prevChildName - $previousChildName")
                    if(snapshot.key != (FirebasePaths.ATD_IS_CONTINUING)) {
                        trySend(snapshot.value.toString()).isSuccess
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildChanged: snapshot - $snapshot, prevChildName - $previousChildName")
                    TODO("Not yet implemented")
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildRemoved: snapshot - $snapshot")
                    TODO("Not yet implemented")
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.i(TAG, "ProfessorTesting_ProRepo - onChildMoved: snapshot - $snapshot, prevChildName - $previousChildName")
                    TODO("Not yet implemented")
                }
            }
        addChildEventListener(childEventListener)
        awaitClose {
            removeEventListener(childEventListener)
        }
    }

}