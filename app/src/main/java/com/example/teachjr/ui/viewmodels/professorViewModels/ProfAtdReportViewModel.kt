package com.example.teachjr.ui.viewmodels.professorViewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.teachjr.data.model.ProfAttendanceDetails
import com.example.teachjr.data.source.repository.ProfRepository
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfAtdReportViewModel
    @Inject constructor(
    private val profRepository: ProfRepository
): ViewModel() {

    private val TAG = ProfAtdReportViewModel::class.java.simpleName

//    private val _studentList = MutableLiveData<Response<List<String>>>()
//    val studentList: LiveData<Response<List<String>>>
//        get() = _studentList
//
//    private val _lecturesList = MutableLiveData<Response<List<Lecture>>>()
//    val lecturesList: LiveData<Response<List<Lecture>>>
//        get() = _lecturesList

    private val _atdDetails = MutableLiveData<Response<ProfAttendanceDetails>>()
    val atdDetails: LiveData<Response<ProfAttendanceDetails>>
        get() = _atdDetails

    suspend fun getAtdDetails(sem_sec: String, courseCode: String) {
       _atdDetails.postValue(Response.Loading())
       withContext(Dispatchers.IO) {
            val stdListResponse = profRepository.getStdList(sem_sec)
            when(stdListResponse) {
                is Response.Loading -> {}
                is Response.Error -> {
                    _atdDetails.postValue(Response.Error(stdListResponse.errorMessage!!, null))
                    Log.i(TAG, "Prof_testing: stdList error - ${stdListResponse.errorMessage}")
                }
                is Response.Success -> {

                    val lecListResponse = profRepository.getLectureList(sem_sec, courseCode)
                    when(lecListResponse) {
                        is Response.Loading -> {}
                        is Response.Error -> {
                            _atdDetails.postValue(Response.Error(lecListResponse.errorMessage!!, null))
                            Log.i(TAG, "Prof_testing: lecList error - ${lecListResponse.errorMessage}")
                        }
                        is Response.Success -> {
                            _atdDetails.postValue(
                                Response.Success(ProfAttendanceDetails(
                                studentList = stdListResponse.data!!,
                                lectureList = lecListResponse.data!!
                            )))
                        }
                    }
                }
            }
       }
    }

//    fun getStdList(sem_sec: String) {
//        _studentList.postValue(Response.Loading())
//        viewModelScope.launch {
//            val stdListResponse = profRepository.getStdList(sem_sec)
//            _studentList.postValue(stdListResponse)
//        }
//    }
//
//    fun getLectureList(sem_sec: String, courseCode: String) {
//        _lecturesList.postValue(Response.Loading())
//        viewModelScope.launch {
//            val lecListResponse = profRepository.getLectureList(sem_sec, courseCode)
//            _lecturesList.postValue(lecListResponse)
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "onCleared: VIEWMODEL CLEARED")
    }
}