package com.example.teachjr.data.source.repository

import com.example.teachjr.utils.Response
import com.example.teachjr.utils.UserType
import com.google.firebase.auth.FirebaseUser

interface AuthRepository {

    suspend fun login(email: String, password: String): Response<FirebaseUser>

    suspend fun signupStudent(
        name: String, enrollment: String, email: String, password: String
    ): Response<FirebaseUser>

    suspend fun signupProfessor(
        name: String, email: String, password: String
    ): Response<FirebaseUser>
}