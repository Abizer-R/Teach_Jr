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

    private val _currUser = MutableLiveData<Response<User>>()
    val currUser: LiveData<Response<User>>
        get() = _currUser

    /**
     * Used by Fragment: Create_Course to recognise the newly created Course
     */
    private val _newCourseID = MutableLiveData<Response<String>>()
    val newCourseID: LiveData<Response<String>>
        get() = _newCourseID

    /**
     * Used by Fragment: Home_Page to Display CourseList in RecyclerView
     */
    private val _courseDocs = MutableLiveData<Response<List<CourseDocument>>>()
    val courseDocs: LiveData<Response<List<CourseDocument>>>
        get() = _courseDocs

    private val _lectureDoc = MutableLiveData<Response<LecturesDocument>>()
    val lectureDoc: LiveData<Response<LecturesDocument>>
        get() = _lectureDoc

    init {
        getUserDetails()
    }

    fun getUserDetails() {
        viewModelScope.launch {
            val user = profRepository.getUserDetails()
            Log.i("TAG", "GET USER DETAILS-ViewModel: ${user.data}")
            _currUser.postValue(user)
        }
    }

    // TODO: Use Flow instead of LiveData
    fun getCourseDocs() {
        _courseDocs.value = Response.Loading()
        viewModelScope.launch {
            val courseListDeferred = async { profRepository.getCourseList() }
            val courseListResponse = courseListDeferred.await()
            when(courseListResponse) {
                is Response.Loading -> {}
                is Response.Error -> _courseDocs.postValue(Response.Error(courseListResponse.errorMessage.toString(), null))
                is Response.Success -> {

                    val courseDocList: MutableList<CourseDocument> = ArrayList<CourseDocument>()
                    for(id in courseListResponse.data!!) {
                        val currDocDeferred = async { profRepository.getCourseDoc(id) }
                        val courseDocResponse = currDocDeferred.await()
                        when(courseListResponse) {
                            is Response.Loading -> _courseDocs.value = Response.Loading()
                            is Response.Error -> _courseDocs.postValue(Response.Error(courseListResponse.errorMessage.toString(), null))
                            else -> {
                                courseDocList.add(courseDocResponse.data!!)
                                // TODO: USE FLOW PLEASEEEEE
                                _courseDocs.postValue(Response.Success(courseDocList))
                                Log.i("TAG", "New data loaded: ${courseDocList}")
                            }
                        }
                    }

                }
            }
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

    fun getRvCourseItemList(courseDocList: List<CourseDocument>): List<RvCourseListItem> {
        val rvCourseList: MutableList<RvCourseListItem> = ArrayList<RvCourseListItem>()

        for(doc in courseDocList) {
            rvCourseList.add(
                RvCourseListItem(courseId = doc.courseId!!, courseName = doc.courseName!!, courseCode = doc.courseCode!!)
            )
        }
        return rvCourseList
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