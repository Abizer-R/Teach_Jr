package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.log

@HiltViewModel
class StudentViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository
): ViewModel() {

//        private val _currUser = MutableLiveData<>

    // TODO: Make a liveData and update views
//    private val _newCourseStatus =


    fun enrollCourse(courseId: String) {
        viewModelScope.launch {
            // TODO: Make a liveData and update views
            val requestReponse = studentRepository.enrollCourse(courseId)

            when(requestReponse) {
                is Response.Loading -> {}
                is Response.Error -> {
                    Log.i("STUDENTVIEWMODEL", "enrollCourse: Error - ${requestReponse.errorMessage}")
                }
                is Response.Success -> {
                    Log.i("STUDENTVIEWMODEL", "enrollCourse: Requested successfully")
                }
            }
        }
    }


}