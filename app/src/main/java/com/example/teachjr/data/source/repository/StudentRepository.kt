package com.example.teachjr.data.source.repository

import android.util.Log
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


    /**
     * This function returns Response.Success() only if request was not made in past
     * Returns Response.Success(true) -> New Request was added successfully
     * Otherwise, it returns corresponding error
     */
    suspend fun enrollCourse(courseId: String): Response<Boolean> {

        try {
            val requestStatus = getReqStatus(courseId)
            when(requestStatus) {
                "TRUE" -> {
                    // See if our req is pending or if professor declined it
                    Log.i(TAG, "enrollCourse: COURSE_REQUEST FOUND")

                    val enrollmentDocId = getEnrollmentDocId(courseId)
                    val isDeclined = isDeclined(enrollmentDocId)

                    if(isDeclined) {
                        Log.i(TAG, "enrollCourse: COURSE_REQ DECLINED")
                        return Response.Error("Request declined by professor", null)
                    } else {
                        Log.i(TAG, "enrollCourse: ALREADY ENROLLED")
                        return Response.Error("Already Requested", null)
                    }
                }

                "FALSE" -> {
                    // Create a new request
                    Log.i(TAG, "enrollCourse: COURSE_REQUEST NOT FOUND")
                    val createRequestResponse = createRequest(courseId)
                    return createRequestResponse
                }

                else -> {
                    // Something went wrong in checking status
                    Log.i(TAG, "enrollCourse: ERROR - $requestStatus")
                    return Response.Error(requestStatus, null)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return Response.Error(e.message.toString(), null)
        }
    }

    suspend fun getReqStatus(courseId: String): String {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(FirebasePaths.COURSE_LIST)
                .child(currentUser.uid)
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

    suspend fun isDeclined(enrollmentDocId: String): Boolean{
        val requestVal =
            dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION)
                .child(enrollmentDocId)
                .child(FirebasePaths.ENROLLMENT_REQ_LIST)
                .child(currentUser.uid)
                .get().await()
        return requestVal.getValue(Boolean::class.java) == false

    }

    suspend fun createRequest(courseId: String): Response<Boolean> {
        val enrollmentDocId = getEnrollmentDocId(courseId)
        val addToCourseListResponse = addReqToCourseList(courseId)

        when(addToCourseListResponse) {
            is Response.Loading -> {}
            is Response.Error -> return addToCourseListResponse
            is Response.Success -> {
                // Adding request to enrollment Request List
                val addToReqListResponse = addToReqList(enrollmentDocId)
                return addToReqListResponse
            }
        }
        return Response.Error("Something went wrong while add a request", null)
    }

    suspend fun getEnrollmentDocId(courseId: String): String {
        val enrollmentDocId =
            dbRef.getReference(FirebasePaths.COURSE_COLLECTION)
                .child(courseId)
                .child(FirebasePaths.ENROLLMENT_DOC_ID)
                .get().await()
        return enrollmentDocId.value.toString()
    }

    suspend fun addReqToCourseList(courseId: String): Response<Boolean> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(FirebasePaths.COURSE_LIST)
                .child(currentUser.uid)
                .child(courseId)
                .setValue(false)
                .addOnSuccessListener {
                    continuation.resume(Response.Success(true))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }

    suspend fun addToReqList(enrollmentDocId: String): Response<Boolean> {
        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION)
                .child(enrollmentDocId)
                .child(FirebasePaths.ENROLLMENT_REQ_LIST)
                .child(currentUser.uid)
                .setValue(true)
                .addOnSuccessListener {
                    continuation.resume(Response.Success(true))
                }
                .addOnFailureListener {
                    continuation.resume(Response.Error(it.message.toString(), null))
                }
        }
    }
}