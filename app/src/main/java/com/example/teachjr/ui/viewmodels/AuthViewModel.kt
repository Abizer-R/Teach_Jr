package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.User
import com.example.teachjr.data.source.repository.AuthRepositoryImpl
import com.example.teachjr.utils.UserType
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject constructor(
    private val authRepository: AuthRepositoryImpl
): ViewModel() {

    private val _userType = MutableLiveData<UserType?>(UserType.Student())
    val userType: LiveData<UserType?>
        get() = _userType

    private val _loginStatus = MutableLiveData<Response<UserType>>()
    val loginStatus: LiveData<Response<UserType>>
        get() = _loginStatus

    private val _signupStatus = MutableLiveData<Response<FirebaseUser>>()
    val signupStatus: LiveData<Response<FirebaseUser>?>
        get() = _signupStatus

    fun setUserType(userString: String) {
        if(userString.equals(FirebaseConstants.TYPE_STUDENT)) {
            Log.i("TAG", "setUserType: USERTYPE Student")
            _userType.postValue(UserType.Student())
        } else if(userString.equals(FirebaseConstants.TYPE_PROFESSOR)){
            Log.i("TAG", "setUserType: USERTYPE Professor")
            _userType.postValue(UserType.Teacher())
        }
    }

    fun login(email: String, password: String) {
        _loginStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = authRepository.login(email, password)

            when(result) {
                is Response.Error -> {
                    _loginStatus.postValue(Response.Error(result.errorMessage.toString(), null))
                }
                is Response.Loading -> {}
                is Response.Success -> {
                    // TODO: Cross Check Enrollment Number
//                    val type = authRepository.getUserType()
//                    _loginStatus.postValue(Response.Success(type))
                }
            }
        }
    }


//    fun signupStudent(name: String, enrollment: String, email: String, password: String) {
//        _signupStatus.postValue(Response.Loading())
//        viewModelScope.launch {
//            val result = authRepository.signupStudent(name, enrollment, email, password)
//            _signupStatus.postValue(result)
//        }
//
//    }

    fun signupProfessor(name: String, email: String, password: String) {
        _signupStatus.postValue(Response.Loading())
        viewModelScope.launch {
            val result = authRepository.signupProfessor(name, email, password)
            _signupStatus.postValue(result)
        }
    }

//    fun logout() {
//        authRepository.logout()
//        _loginStatus.value = null
//        _signupStatus.value = null
//    }

}