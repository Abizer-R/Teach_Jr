package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.User
import com.example.teachjr.data.source.repository.StudentEnrollRepository
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class StudentViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository,
        private val studentEnrollRepository: StudentEnrollRepository
): ViewModel() {

    private val _currUser = MutableLiveData<Response<User>>()
    val currUser: LiveData<Response<User>>
        get() = _currUser

    private val _enrollCourseStatus = MutableLiveData<Response<Boolean>>()
    val enrollCourseStatus: LiveData<Response<Boolean>>
        get() = _enrollCourseStatus



    // TODO: Make a liveData and update views
//    private val _newCourseStatus =

    init {
        getUserDetails()
    }

    private fun getUserDetails() {
        viewModelScope.launch {
            val user = studentRepository.getUserDetails()
            Log.i("TAG", "GET USER DETAILS-ViewModel: ${user.data}")
            _currUser.postValue(user)
        }
    }


    fun enrollCourse(courseId: String) {
        _enrollCourseStatus.postValue(Response.Loading())
        viewModelScope.launch {
            val userId = currUser.value?.data?.id

            if(userId != null) {
                val requestReponse = studentEnrollRepository.enrollCourse(courseId, _currUser.value?.data?.id!!)
                _enrollCourseStatus.postValue(requestReponse)
            } else {
                _enrollCourseStatus.postValue(Response.Error("Cannot extract user. Please try again.", null))
            }
        }
    }


}