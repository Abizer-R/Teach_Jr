package com.example.teachjr.ui.viewmodels

import android.util.Log
import android.widget.Toast
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject constructor(
    private val authRepository: AuthRepositoryImpl
): ViewModel() {

    private val _userType = MutableLiveData<UserType?>(UserType.Student())
    val userType: LiveData<UserType?>
        get() = _userType

    private val _loginStatus = MutableLiveData<Response<FirebaseUser>>()
    val loginStatus: LiveData<Response<FirebaseUser>>
        get() = _loginStatus


    fun setUserType(userString: String) {
        if(userString.equals(FirebaseConstants.TYPE_STUDENT)) {
            _userType.postValue(UserType.Student())
        } else if(userString.equals(FirebaseConstants.TYPE_PROFESSOR)){
            _userType.postValue(UserType.Teacher())
        }
    }

    fun loginProfessor(email: String, password: String) {
        _loginStatus.value = Response.Loading()
        viewModelScope.launch {
            val result = async { authRepository.login(email, password) }
            when(val response = result.await()) {
                is Response.Error -> {
                    _loginStatus.postValue(Response.Error(response.errorMessage.toString(), null))
                }
                is Response.Loading -> {}
                is Response.Success -> {
                    // Verifies if user is professor/student and updates _loginStatus
                    verifyAndUpdate(FirebaseConstants.TYPE_PROFESSOR)
                }
            }
        }
    }

    private suspend fun verifyAndUpdate(reqUserType: String) {
        withContext(Dispatchers.IO) {
            val userType = async { authRepository.getUserType() }
            when(val typeResponse = userType.await()) {
                is Response.Error -> _loginStatus.postValue(Response.Error(typeResponse.errorMessage.toString(), null))
                is Response.Loading -> {}
                is Response.Success -> {
                    if(typeResponse.data.equals(reqUserType)) {
                        _loginStatus.postValue(Response.Success(authRepository.currUser))
                    } else {
                        _loginStatus.postValue(Response.Error("This credentials does not belong to a $reqUserType", null))
                    }
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

//    fun signupProfessor(name: String, email: String, password: String) {
//        _signupStatus.postValue(Response.Loading())
//        viewModelScope.launch {
//            val result = authRepository.signupProfessor(name, email, password)
//            _signupStatus.postValue(result)
//        }
//    }

    fun logout() {
        authRepository.logout()
        _loginStatus.postValue(Response.Error("LOGGED_OUT", null))
    }

}