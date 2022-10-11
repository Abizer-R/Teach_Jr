package com.example.teachjr.data.source.repository

import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl
    @Inject constructor(
    private val firebaseAuth: FirebaseAuth
): AuthRepository {

    override val currUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun login(email: String, password: String): Response<FirebaseUser> {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            return Response.Success(result.user)
        } catch (e: Exception) {
            e.printStackTrace()
            return Response.Error(e.message.toString())
        }
    }

    override suspend fun signup(email: String, password: String): Response<FirebaseUser> {
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            return Response.Success(result.user)
        } catch (e: Exception) {
            e.printStackTrace()
            return Response.Error(e.message.toString())
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }

}