package com.example.teachjr.utils

sealed class UserType {
    class Student: UserType()
    class Teacher: UserType()
}
