package com.example.teachjr.data.model

data class LectureCollection constructor(
    val lecCount: Int? = 0,
    val lecDocumentList: List<LectureDocument>? = ArrayList()
)
