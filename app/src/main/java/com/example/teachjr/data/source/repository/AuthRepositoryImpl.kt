package com.example.teachjr.data.source.repository

import android.util.Log
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.teachjr.data.model.User
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebaseConstants.userType
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import com.example.teachjr.utils.UserType
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.inject.Deferred
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl
    @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {

    val currUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    suspend fun login(email: String, password: String): Response<FirebaseUser> {
        try {
            // We have no use of result right now...
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            return Response.Success(result.user)
        } catch (e: Exception) {
            e.printStackTrace()
            return Response.Error(e.message.toString(), null)
        }
    }

//    /**
//     * Same code is used in SplashRepository for getting User type
//     */
//    suspend fun getUserType(): UserType {
//        val result = dbRef.getReference(FirebasePaths.USER_COLLECTION)
//            .child(firebaseAuth.currentUser!!.uid)
//            .singleValueEvent()
//
//        if(result.equals(FirebaseConstants.TYPE_STUDENT)) {
//            Log.i("TAG", "getUserType: Student Confirmed")
//            return UserType.Student()
//        } else {
//            Log.i("TAG", "getUserType: Professor Confirmed")
//            return UserType.Teacher()
//        }
//    }

//    private suspend fun DatabaseReference.singleValueEvent(): String = suspendCoroutine { continuation ->
//        val valueEventListener = object: ValueEventListener {
//            override fun onCancelled(error: DatabaseError) { }
//
//            override fun onDataChange(snapshot: DataSnapshot) {
//                Log.i("TAG", "onDataChange: snapshot: $snapshot")
//                val userType = snapshot.child(FirebaseConstants.userType).value.toString()
//                Log.i("TAG", "onDataChange: Usertype: $userType")
//                continuation.resume(userType)
//            }
//        }
//        addListenerForSingleValueEvent(valueEventListener) // Subscribe to the event
//    }

//    suspend fun signupStudent(
//        name: String, enrollment: String, email: String, password: String
//    ): Response<FirebaseUser> {
//        try {
//            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
//
//            if(result.user == null) {
//                return Response.Error("Null User", null)
//            } else {
//                val id = result.user!!.uid
//                val userStd = User(
//                    id = id,
//                    name = name,
//                    userType = FirebaseConstants.TYPE_STUDENT,
//                    enrollment = enrollment
//                )
//                dbRef.getReference(FirebasePaths.USER_COLLECTION)
//                    .child(FirebasePaths.USER_INFO)
//                    .child(id)
//                    .setValue(userStd)
//                    .await()
//
//                return Response.Success(result.user)
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            return Response.Error(e.message.toString(), null)
//        }
//    }

    suspend fun getUserType(): Response<String> {

        return suspendCoroutine { continuation ->
            dbRef.getReference(FirebasePaths.USER_COLLECTION)
                .child(currUser!!.uid)
                .child(FirebaseConstants.userType)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        continuation.resume(Response.Success(snapshot.value.toString()))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Response.Error(error.message, null))
                    }
                })
        }
    }


    fun logout() {
        firebaseAuth.signOut()
    }

}