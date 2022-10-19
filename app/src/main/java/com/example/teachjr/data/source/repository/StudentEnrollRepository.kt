package com.example.teachjr.data.source.repository

import android.os.Build.VERSION_CODES.P
import android.util.Log
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.FirebasePaths.COURSE_LIST
import com.example.teachjr.utils.FirebasePaths.ENROLLMENT_COLLECTION
import com.example.teachjr.utils.FirebasePaths.ENROLLMENT_REQ_COUNT
import com.example.teachjr.utils.FirebasePaths.ENROLLMENT_REQ_LIST
import com.example.teachjr.utils.FirebasePaths.USER_COLLECTION
import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class StudentEnrollRepository
@Inject constructor(
    private val dbRef: FirebaseDatabase
) {

    private val TAG = StudentRepository::class.java.simpleName

    suspend fun getCourseDetails() {

    }


    /**
     * This function returns Response.Success() only if request was not made in past
     * Returns Response.Success(true) -> New Request was added successfully
     * Returns Response.Success(false) -> Already requested to enroll in this course
     * Otherwise, it returns corresponding error
     */
    suspend fun enrollCourse(courseId: String, userId: String): Response<Boolean> {

        try {
            val requestStatus = getReqStatus(courseId, userId)
            when(requestStatus) {
                "TRUE" -> {
                    // See if our req is pending or if professor declined it
                    Log.i(TAG, "enrollCourse: COURSE_REQUEST FOUND")

                    val enrollmentDocId = getEnrollmentDocId(courseId)
                    val isDeclined = isDeclined(enrollmentDocId, userId)

                    if(isDeclined) {
                        Log.i(TAG, "enrollCourse: COURSE_REQ DECLINED")
                        return Response.Error("Request declined by professor", null)
                    } else {
                        Log.i(TAG, "enrollCourse: ALREADY ENROLLED")
                        return Response.Success(false)
                    }
                }

                "FALSE" -> {
                    // Create a new request
                    Log.i(TAG, "enrollCourse: COURSE_REQUEST NOT FOUND")
                    val createRequestResponse = createRequest(courseId, userId)
                    return createRequestResponse
                }

                else -> {
                    // Something went wrong in checking status
                    Log.i(TAG, "enrollCourse: ERROR - $requestStatus")
                    return Response.Error(requestStatus, null)
                }
            }

        } catch (e: Exception) {
            Log.i(TAG, "enrollCourse: Exception: ${e.message}")
            e.printStackTrace()
            return Response.Error(e.message.toString(), null)
        }
    }

    suspend fun getReqStatus(courseId: String, userId: String): String {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(FirebasePaths.COURSE_LIST)
                .child(userId)
                .child(courseId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()) {
                            continuation.resume("TRUE")
                        } else {
                            continuation.resume("FALSE")
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(error.message)
                    }
                })
        }
    }

    suspend fun isDeclined(enrollmentDocId: String, userId: String): Boolean{
        val requestVal =
            dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION)
                .child(enrollmentDocId)
                .child(FirebasePaths.ENROLLMENT_REQ_LIST)
                .child(userId)
                .get().await()
        return requestVal.getValue(Boolean::class.java) == false

    }

    suspend fun createRequest(courseId: String, userId: String): Response<Boolean> {
        val enrollmentDocId = getEnrollmentDocId(courseId)

        val updates: MutableMap<String, Any> = hashMapOf(
            "/$USER_COLLECTION/$COURSE_LIST/$userId/$courseId" to false,
            "/$ENROLLMENT_COLLECTION/$enrollmentDocId/$ENROLLMENT_REQ_LIST/$userId" to true,
        )

        return suspendCoroutine { continuation ->
            dbRef.reference.updateChildren(updates)
                .addOnSuccessListener {
                    continuation.resume(Response.Success(true))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }

    suspend fun getEnrollmentDocId(courseId: String): String {
        val enrollmentDocId =
            dbRef.getReference(FirebasePaths.COURSE_COLLECTION)
                .child(courseId)
                .child(FirebasePaths.ENROLLMENT_DOC_ID)
                .get().await()
        return enrollmentDocId.value.toString()
    }

    suspend fun incrementCount(enrollmentDocId: String): String {
        return suspendCoroutine { continuation ->
            dbRef.getReference("/$ENROLLMENT_COLLECTION/$enrollmentDocId/$ENROLLMENT_REQ_COUNT")
                .runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        val count = mutableData.getValue(Int::class.java)
                            ?: return Transaction.success(mutableData)

                        // Set value and report transaction success
                        mutableData.value = count + 1
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(
                        databaseError: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        // Transaction completed
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError?.message)
                        if(databaseError?.message == null) {
                            continuation.resume("SUCCESSFUL")
                        } else {

                            continuation.resume(databaseError.message)
                        }
                    }
                })
        }
    }

//    suspend fun addReqToCourseList(courseId: String, userId: String): Response<Boolean> {
//        return suspendCoroutine { continuation ->
//            dbRef.getReference(FirebasePaths.USER_COLLECTION)
//                .child(FirebasePaths.COURSE_LIST)
//                .child(userId)
//                .child(courseId)
//                .setValue(false)
//                .addOnSuccessListener {
//                    continuation.resume(Response.Success(true))
//                }
//                .addOnFailureListener {
//                    continuation.resume(Response.Error(it.message.toString(), null))
//                }
//        }
//    }
//
//    suspend fun addToReqList(enrollmentDocId: String, userId: String): Response<Boolean> {
//        return suspendCoroutine { continuation ->
//            dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION)
//                .child(enrollmentDocId)
//                .child(FirebasePaths.ENROLLMENT_REQ_LIST)
//                .child(userId)
//                .setValue(true)
//                .addOnSuccessListener {
//                    continuation.resume(Response.Success(true))
//                }
//                .addOnFailureListener {
//                    continuation.resume(Response.Error(it.message.toString(), null))
//                }
//        }
//    }
}