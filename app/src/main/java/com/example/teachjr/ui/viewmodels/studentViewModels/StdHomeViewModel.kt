package com.example.teachjr.ui.viewmodels.studentViewModels

import android.util.Log
import androidx.lifecycle.*
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class StdHomeViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository
): ViewModel() {

    private val TAG = StdHomeViewModel::class.java.simpleName

    private val _currUserStd = MutableLiveData<Response<StudentUser>>()
    val currUserStd: LiveData<Response<StudentUser>>
        get() = _currUserStd

    init {
        getUser()
    }

    private fun getUser() {
        _currUserStd.postValue(Response.Loading())
        viewModelScope.launch {
            _currUserStd.postValue(studentRepository.getUserDetails())
        }
    }

    private val _courseList = MutableLiveData<Response<List<RvStdCourseListItem>>>()
    val courseList: LiveData<Response<List<RvStdCourseListItem>>>
        get() = _courseList

    fun getCourseList() {
        _courseList.postValue(Response.Loading())
        Log.i(TAG, "StdTesting-ViewModel: Calling getCourselist")
        viewModelScope.launch {
            val institute = currUserStd.value?.data?.institute
            val branch = currUserStd.value?.data?.branch
            val semSec = currUserStd.value?.data?.sem_sec
            if(institute == null || branch == null || semSec == null) {
                _courseList.postValue(Response.Error("UserDetails are null, try again", null))
            } else {
                val courseListDeferred = async { studentRepository.getCourseList(institute, branch, semSec) }
                _courseList.postValue(courseListDeferred.await())
            }
        }
    }

}