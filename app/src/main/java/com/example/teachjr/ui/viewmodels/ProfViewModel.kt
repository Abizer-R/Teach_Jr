package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.LecturesDocument
import com.example.teachjr.data.model.RvProfCourseListItem
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ProfViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    /**
     * Used by Fragment: Home_Page to Display CourseList in RecyclerView
     */
    private val _courseList = MutableLiveData<Response<List<RvProfCourseListItem>>>()
    val courseList: LiveData<Response<List<RvProfCourseListItem>>>
        get() = _courseList

    private val _lecCount = MutableLiveData<Response<Int>>()
    val lecCount: LiveData<Response<Int>>
        get() = _lecCount

    private val _atdList = MutableLiveData<Response<List<String>>>()
    val atdList: LiveData<Response<List<String>>>
        get() = _atdList

    // TODO: Use Flow instead of LiveData
    fun getCourseList() {
        _courseList.postValue(Response.Loading())
        viewModelScope.launch {
            val courseListDeferred = async { profRepository.getCourseList() }
            _courseList.postValue(courseListDeferred.await())
        }
    }

    fun getLectureCount(sem_sec: String, courseCode: String) {
        _lecCount.postValue(Response.Loading())
        viewModelScope.launch {
            val lecCountDeferred = async { profRepository.getLectureCount(sem_sec, courseCode) }
            _lecCount.postValue(lecCountDeferred.await())
        }
    }

    fun createNewAtdRef(sem_sec: String, courseCode: String, lecCount: Int) {
        _atdList.postValue(Response.Loading())
        viewModelScope.launch {
            val currTime = Calendar.getInstance().timeInMillis
            val newLecDocDeferred = async { profRepository.createNewAtdRef(sem_sec, courseCode, currTime, lecCount) }
            when(val newLecCreated = newLecDocDeferred.await()) {
                is Response.Loading -> {}
                is Response.Error -> _atdList.postValue(Response.Error(newLecCreated.errorMessage.toString(), null))
                is Response.Success -> {
                    // TODO: Start Service broadcast and share timestamp (using it as otp for now)
                    if(newLecCreated.data == true) {
                        Log.i("TAG", "ProfessorTesting_ViewModel: node created")
                    }
                }
            }
         }
    }

}