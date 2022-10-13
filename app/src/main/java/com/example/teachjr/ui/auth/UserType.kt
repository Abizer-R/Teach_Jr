package com.example.teachjr.ui.auth

sealed class UserType() {
    class Student: UserType()
    class Teacher: UserType()
}
