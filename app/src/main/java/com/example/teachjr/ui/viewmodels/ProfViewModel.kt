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
        _presentList.postValue(Response.Loading())
        viewModelScope.launch {

            val currTime = Calendar.getInstance().timeInMillis
            val newLecDocDeferred = async { profRepository.createNewAtdRef(sem_sec, courseCode, currTime, lecCount) }
            when(val newLecCreated = newLecDocDeferred.await()) {
                is Response.Loading -> {}
                is Response.Error -> _presentList.postValue(Response.Error(newLecCreated.errorMessage.toString(), null))
                is Response.Success -> {
                    // TODO: Start Service broadcast and share timestamp (using it as otp for now)

                    val stdList: MutableList<String> = ArrayList()
                    try {
                        withContext(Dispatchers.IO) {
                            profRepository.observeAttendance(sem_sec, courseCode, "LEC_${lecCount+1}")
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
            }
         }
    }

}