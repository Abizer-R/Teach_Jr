package com.example.teachjr.data.model

data class User constructor(
    val id: String? = "",
    val name: String? = "",
    val userType: String? = "",
    val enrollment: String? = null,
    val courseList: List<String>? = ArrayList()
)