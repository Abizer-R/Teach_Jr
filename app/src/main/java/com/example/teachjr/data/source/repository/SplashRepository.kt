package com.example.teachjr.data.source.repository

import android.util.Log
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.UserType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SplashRepository
@Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val dbRef: FirebaseDatabase
) {
    val currUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    /**
     * Same code is used in AuthRepository for getting User type
     */
    suspend fun getUserType(): UserType {
        val result = dbRef.getReference(FirebasePaths.USER_COLLECTION)
            .child(FirebasePaths.USER_INFO)
            .child(firebaseAuth.currentUser!!.uid)
            .singleValueEvent()

        if(result.equals(FirebaseConstants.TYPE_STUDENT)) {
            Log.i("TAG", "getUserType: Student Confirmed")
            return UserType.Student()
        } else {
            Log.i("TAG", "getUserType: Professor Confirmed")
            return UserType.Teacher()
        }
    }

    suspend fun DatabaseReference.singleValueEvent(): String = suspendCoroutine { continuation ->
        val valueEventListener = object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) { }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i("TAG", "onDataChange: snapshot: $snapshot")
                val userType = snapshot.child(FirebaseConstants.userType).value.toString()
                Log.i("TAG", "onDataChange: Usertype: $userType")
                continuation.resume(userType)
            }
        }
        addListenerForSingleValueEvent(valueEventListener) // Subscribe to the event
    }
}