package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.CourseDocument
import com.example.teachjr.data.model.LecturesDocument
import com.example.teachjr.data.model.RvCourseListItem
import com.example.teachjr.data.model.User
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

//    private val _currUser = MutableLiveData<Response<User>>()
//    val currUser: LiveData<Response<User>>
//        get() = _currUser

    /**
     * Used by Fragment: Create_Course to recognise the newly created Course
     */
    private val _newCourseID = MutableLiveData<Response<String>>()
    val newCourseID: LiveData<Response<String>>
        get() = _newCourseID

    /**
     * Used by Fragment: Home_Page to Display CourseList in RecyclerView
     */
    private val _courseList = MutableLiveData<Response<List<RvCourseListItem>>>()
    val courseList: LiveData<Response<List<RvCourseListItem>>>
        get() = _courseList
//    private val _courseDocs = MutableLiveData<Response<List<CourseDocument>>>()
//    val courseDocs: LiveData<Response<List<CourseDocument>>>
//        get() = _courseDocs

    private val _lecCount = MutableLiveData<Response<Int>>()
    val lecCount: LiveData<Response<Int>>
        get() = _lecCount

    private val _lectureDoc = MutableLiveData<Response<LecturesDocument>>()
    val lectureDoc: LiveData<Response<LecturesDocument>>
        get() = _lectureDoc

//    init {
//        getUserDetails()
//    }

//    private fun getUserDetails() {
//        viewModelScope.launch {
//            val user = profRepository.getUserDetails()
//            Log.i("TAG", "GET USER DETAILS-ViewModel: ${user.data}")
//            _currUser.postValue(user)
//        }
//    }

    // TODO: Use Flow instead of LiveData
    fun getCourseDocs() {
        _courseList.postValue(Response.Loading())
        viewModelScope.launch {
            val courseListDeferred = async { profRepository.getCourseList() }
            _courseList.postValue(courseListDeferred.await())
        }
    }

    fun getLectureCount(sem_sec: String, courseCode: String) {
        _lectureDoc.postValue(Response.Loading())
        viewModelScope.launch {
            val lecCountDeferred = async { profRepository.getLectureCount(sem_sec, courseCode) }
            _lecCount.postValue(lecCountDeferred.await())
        }
    }

    fun getLectureDoc(courseId: String) {
        _lectureDoc.postValue(Response.Loading())
        viewModelScope.launch {
            val lecDocResponse = profRepository.getLectureDoc(courseId)
            when(lecDocResponse) {
                is Response.Loading -> {}
                is Response.Error -> _lectureDoc.postValue(Response.Error(lecDocResponse.errorMessage.toString(), null))
                is Response.Success -> {
                    _lectureDoc.postValue(Response.Success(lecDocResponse.data))
                    Log.i("TAG", "Lecture Doc: ${lecDocResponse.data}")
                }
            }
        }
    }
}