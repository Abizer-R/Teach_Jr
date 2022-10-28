package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StdAttendanceDetails
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository
): ViewModel() {

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

//    private val _enrollCourseStatus = MutableLiveData<Response<Boolean>>()
//    val enrollCourseStatus: LiveData<Response<Boolean>>
//        get() = _enrollCourseStatus

    private val _courseList = MutableLiveData<Response<List<RvStdCourseListItem>>>()
    val courseList: LiveData<Response<List<RvStdCourseListItem>>>
        get() = _courseList

    // TODO: Make an object StdLecInfo(lecNumber, timestamp, isPresent) and pass it in here
    private val _atdDetails = MutableLiveData<Response<StdAttendanceDetails>>()
    val atdDetails: LiveData<Response<StdAttendanceDetails>>
        get() = _atdDetails

    private val _markAtdStatus = MutableLiveData<Response<Boolean>>()
    val markAtdStatus: LiveData<Response<Boolean>>
        get() = _markAtdStatus

    fun getCourseList() {
        _courseList.postValue(Response.Loading())
        Log.i("TAG", "StdTesting-ViewModel: Calling getCourselist")
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

    fun getAttendanceDetails(courseCode: String?) {
        _atdDetails.postValue(Response.Loading())
        Log.i("TAG", "StdTesting-ViewModel: Calling getAttendanceDetails")
        viewModelScope.launch {
            val semSec = currUserStd.value?.data?.sem_sec
            if(courseCode == null || semSec == null) {
                Log.i("TAG", "StdTesting-ViewModel: Error - null values")
                _atdDetails.postValue(Response.Error("Error - null values", null))
            } else {
                _atdDetails.postValue(studentRepository.getAttendanceDetails(semSec, courseCode))
            }
        }
    }

    fun martAtd(courseCode: String, timestamp: String) {
        _markAtdStatus.postValue(Response.Loading())
        Log.i("TAG", "StdTesting-ViewModel: Calling markAtd")
        viewModelScope.launch {
            val semSec = currUserStd.value?.data?.sem_sec
            val enrollment = currUserStd.value?.data?.enrollment
            if(semSec == null || enrollment == null) {
                Log.i("TAG", "StdTesting-ViewModel: marAtd() Error - null values")
            } else {
                _markAtdStatus.postValue(studentRepository.markAtd(semSec, courseCode, timestamp, enrollment))
            }
        }
    }
//    fun enrollCourse(courseId: String) {
//        _enrollCourseStatus.postValue(Response.Loading())
//        viewModelScope.launch {
//            val userId = currUser.value?.data?.id
//
//            if(userId != null) {
//                val requestReponse = studentEnrollRepository.enrollCourse(courseId, _currUser.value?.data?.id!!)
//                _enrollCourseStatus.postValue(requestReponse)
//            } else {
//                _enrollCourseStatus.postValue(Response.Error("Cannot extract user. Please try again.", null))
//            }
//        }
//    }

    


}