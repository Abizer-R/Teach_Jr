package com.example.teachjr.ui.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.source.repository.SplashRepository
import com.example.teachjr.utils.Response
import com.example.teachjr.utils.UserType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject constructor(
    private val splashRepository: SplashRepository
): ViewModel() {

    private val _userType = MutableLiveData<UserType?>()
    val userType: LiveData<UserType?>
        get() = _userType

    init {
        // TODO: REMOVE THIS
        splashRepository.logout()
        _userType.postValue(null)


//        Log.i("TAG", "Testing: USERID = ${splashRepository.currUser?.uid}")
//        if(splashRepository.currUser != null) {
//            viewModelScope.launch {
//                splashRepository.getUserType()
//            }
//        } else {
//            _userType.postValue(null)
//        }
    }

    fun getUserType() {

    }
}