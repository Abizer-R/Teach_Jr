package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.User
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    private val _currUser = MutableLiveData<Response<User>>()
    val currUser: LiveData<Response<User>>
        get() = _currUser

    init {
        getUserDetails()
    }

    private val _newCourseID = MutableLiveData<Response<String>>()
    val newCourseID: LiveData<Response<String>>
        get() = _newCourseID

    fun getUserDetails() {
        viewModelScope.launch {
            val user = profRepository.getUserDetails()
            Log.i("TAG", "GET USER DETAILS-ViewModel: ${user.data}")
            _currUser.postValue(user)
        }
    }

    fun createCourse(courseCode: String, courseName: String) {
        _newCourseID.postValue(Response.Loading())
        viewModelScope.launch {
            val profName = currUser.value?.data?.name
            if(profName != null) {
                val courseIDResponse = profRepository.createCourse(courseCode, courseName, profName)
                _newCourseID.postValue(courseIDResponse)
            } else {
                _newCourseID.postValue(Response.Error("Cannot extract user. Please try again.", null))
            }
        }
    }
}