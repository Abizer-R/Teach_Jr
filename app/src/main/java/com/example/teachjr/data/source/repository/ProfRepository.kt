package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.data.model.CourseDocument
import com.example.teachjr.data.model.EnrollmentDocument
import com.example.teachjr.data.model.LecturesDocument
import com.example.teachjr.data.model.User
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

    suspend fun createCourse(
        courseCode: String, courseName: String, profName: String): Response<String> {
        try {

            val lecDocRef = dbRef.getReference(FirebasePaths.LECTURE_COLLECTION).push()
            val enrolDocRef = dbRef.getReference(FirebasePaths.ENROLLMENT_COLLECTION).push()

            val newCourse: CourseDocument = CourseDocument(
                courseCode = courseCode,
                courseName = courseName,
                profId = currentUser.uid,
                profName = profName,
                enrolDocId = enrolDocRef.key,
                lecDocId = lecDocRef.key
            )

            val courseRef = dbRef.getReference(FirebasePaths.COURSE_COLLECTION).push()
            courseRef.setValue(newCourse).await()

            lecDocRef.setValue(LecturesDocument(courseId = courseRef.key)).await()
            enrolDocRef.setValue(EnrollmentDocument(courseId = courseRef.key)).await()

            // Adding course ID to professor's User Info
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(FirebasePaths.COURSE_LIST)
                .child(currentUser.uid)
                .child(courseRef.key.toString()).setValue(true).await()
            // Adding course ID to Lectures Collentions


//            // Not exposing the direct ID in order to enhance security
//            return Response.Success("$courseCode/${courseRef.key}")
            return Response.Success(courseRef.key)
        } catch (e: Exception) {
            e.printStackTrace()
            return Response.Error(e.message.toString(), null)
        }
    }

    suspend fun getCourseList(): Response<List<String>> {
        return dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(FirebasePaths.COURSE_LIST)
                .child(currentUser.uid)
                .getCourseIDs()
    }

    // TODO: Change SingleValueEventListener to onDataChangeListener
    private suspend fun DatabaseReference.getCourseIDs(): Response<List<String>> = suspendCoroutine { continuation ->
        val valueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val idList: MutableList<String> = ArrayList()
                    for(id in snapshot.children) {
                        idList.add(id.key.toString())
                    }

                    continuation.resume(Response.Success(idList))
                } else {
                    Log.i(TAG, "onDataChange-getCourseIDs: Something Went Wrong")
                    continuation.resume(Response.Error("Something Went Wrong in repository.getCourseIDs()", null))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resume(Response.Error(error.message, null))
            }
        }
        // Subscribe to the callback
        addListenerForSingleValueEvent(valueEventListener)
    }

    suspend fun getCourseDoc(id: String): Response<CourseDocument> {
        return dbRef.getReference(FirebasePaths.COURSE_COLLECTION)
            .child(id)
            .getCourseDocument()
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