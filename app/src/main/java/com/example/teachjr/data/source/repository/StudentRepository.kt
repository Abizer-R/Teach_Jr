package com.example.teachjr.data.source.repository

import android.util.Log
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

}