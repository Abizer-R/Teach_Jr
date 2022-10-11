package com.example.teachjr.utils

sealed class Response<T>(val data: T? = null, errorMessage: String? = null)
{
    class Loading<T> : Response<T>()
    class Error<T> (errorMessage: String) : Response<T>(errorMessage = errorMessage)
    class Success<T> (data: T? = null) : Response<T> (data = data)
}