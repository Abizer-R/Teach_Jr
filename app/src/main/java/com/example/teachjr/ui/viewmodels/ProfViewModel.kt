package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.model.LecturesDocument
import com.example.teachjr.data.model.RvProfCourseListItem
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.AttendanceStatus
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

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

//    private val _stdList = MutableLiveData<Response<List<String>>>()
//    val stdList: LiveData<Response<List<String>>>
//        get() = _stdList

    private val _atdStatus = MutableLiveData<AttendanceStatus>()
    val atdStatus: LiveData<AttendanceStatus>
        get() = _atdStatus

    private val _presentList = MutableLiveData<Response<List<String>>>()
    val presentList: LiveData<Response<List<String>>>
        get() = _presentList

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

//    fun getStdList(sem_sec: String) {
//        _stdList.postValue(Response.Loading())
//        viewModelScope.launch {
//            val stdListResponse = profRepository.getStdList(sem_sec)
//            _stdList.postValue(stdListResponse)
//        }
//    }


    fun initAtd(sem_sec: String, courseCode: String, lecCount: Int) {
        _atdStatus.postValue(AttendanceStatus.FetchingTimestamp())
        viewModelScope.launch {
            val timestamp = Calendar.getInstance().timeInMillis.toString()
            val newLecDocDeferred = async { profRepository.createNewAtdRef(sem_sec, courseCode, timestamp, lecCount) }
            when(val newLecCreated = newLecDocDeferred.await()) {
                is Response.Loading -> {}
                is Response.Error -> _atdStatus.postValue(AttendanceStatus.Error(newLecCreated.errorMessage.toString()))
                is Response.Success -> {
                    _atdStatus.postValue(AttendanceStatus.Initiated(timestamp))
                }
            }
         }
    }

    /**
     * To understand why we are using 'suspend fun',
     * read the note in ProfMarkAtdFrag (in setupObserver())
     */
    suspend fun observeAtd(sem_sec: String, courseCode: String, timestamp: String) {
        _presentList.postValue(Response.Loading())
        try {
            withContext(Dispatchers.IO) {
                val stdList: MutableList<String> = ArrayList()
                profRepository.observeAttendance(sem_sec, courseCode, timestamp)
                    .collect { student ->
                        stdList.add(student)
                        _presentList.postValue(Response.Success(stdList))
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _presentList.postValue(Response.Error(e.message.toString(), null))
        }
    }

//    fun observeAtd(sem_sec: String, courseCode: String, timestamp: String) {
//        _presentList.postValue(Response.Loading())
//        viewModelScope.launch {
//            val stdList: MutableList<String> = ArrayList()
//            try {
//                withContext(Dispatchers.IO) {
//                    profRepository.observeAttendance(sem_sec, courseCode, timestamp)
//                        .collect { student ->
//                            stdList.add(student)
//                            _presentList.postValue(Response.Success(stdList))
//                        }
//                }
//
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                _presentList.postValue(Response.Error(e.message.toString(), null))
//            }
//        }
//    }



    fun endAttendance(sem_sec: String, courseCode: String, timestamp: String) {

    }

}