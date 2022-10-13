package com.example.teachjr.ui.auth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.source.repository.AuthRepositoryImpl
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.Response
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject constructor(
    private val authRepository: AuthRepositoryImpl
): ViewModel() {

     val currUser: FirebaseUser?
        get() = authRepository.currUser

    private val _userType = MutableLiveData<UserType>(UserType.Student())
    val userType: LiveData<UserType>
        get() = _userType

    private val _loginStatus = MutableLiveData<Response<FirebaseUser>?>(null)
    val loginStatus: LiveData<Response<FirebaseUser>?>
        get() = _loginStatus

    private val _signupStatus = MutableLiveData<Response<FirebaseUser>?>(null)
    val signupStatus: LiveData<Response<FirebaseUser>?>
        get() = _signupStatus

    init {
        if(authRepository.currUser != null) {
            _loginStatus.value = Response.Success(authRepository.currUser)
        }
    }

    fun setUserType(userString: String) {
        if(userString.equals(FirebaseConstants.typeStudent)) {
            _userType.postValue(UserType.Student())
        } else if(userString.equals(FirebaseConstants.typeProfessor)){
            _userType.postValue(UserType.Teacher())
        }
    }

    fun login(email: String, password: String) {
        _loginStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = authRepository.login(email, password)
            _loginStatus.value = result
        }
    }

    fun signup(email: String, password: String) {
        _signupStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = authRepository.signup(email, password)
            _signupStatus.value = result
        }
    }

    fun logout() {
        authRepository.logout()
        _loginStatus.value = null
        _signupStatus.value = null
    }

}