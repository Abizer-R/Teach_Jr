package com.example.teachjr.data.source.repository

import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    val currUser: FirebaseUser?

    suspend fun login(email: String, password: String): Response<FirebaseUser>

    suspend fun signup(email: String, password: String): Response<FirebaseUser>

    fun logout()
}