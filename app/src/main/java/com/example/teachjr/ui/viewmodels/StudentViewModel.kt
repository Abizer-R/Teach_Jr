package com.example.teachjr.ui.viewmodels

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import androidx.lifecycle.*
import com.example.teachjr.data.model.RvStdCourseListItem
import com.example.teachjr.data.model.StdAttendanceDetails
import com.example.teachjr.data.model.StudentUser
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.*
import com.example.teachjr.utils.WifiSD.BroadcastService
import com.example.teachjr.utils.WifiSD.DiscoverService
import com.google.firebase.auth.ktx.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class StudentViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository
): ViewModel() {

    private val TAG = StudentViewModel::class.java.simpleName

    private val serviceList = mutableMapOf<String, String>()
    private var serviceRequest: WifiP2pDnsSdServiceRequest? = null

    private var _isDiscovering = false
    val isDiscovering: Boolean
        get() = _isDiscovering
    fun updateIsDiscovering(newState: Boolean) {_isDiscovering = newState}
//    private val _serviceList = mutableMapOf<String, String>()
//    val serviceList: Map<String, String>
//        get() = _serviceList
//
//    fun addService(key: String, value: String) {
//        _serviceList[key] = value
//    }

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

    private val _atdStatus = MutableLiveData<AttendanceStatusStd>()
    val atdStatus: LiveData<AttendanceStatusStd>
        get() = _atdStatus

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

    // TODO: DUDE, JUST APPLY ABSTRACTION and UPDATE STATUS OF _atdStatus ALONG THE WAY

    fun discoverTimestamp(
        manager: WifiP2pManager, channel: WifiP2pManager.Channel, serviceInstance: String) {

        Log.i("TAG", "WIFI_SD_Testing-ViewModel: discoverTimestamp() Called")
//        _atdStatus.postValue(AttendanceStatusStd.InitiatingDiscovery())
        _atdStatus.postValue(AttendanceStatusStd.DiscoveringTimestamp())
        viewModelScope.launch {
            setServiceRequest(manager, channel, serviceInstance)
            discoverTimestampFor1Min(manager, channel, 0)
        }
    }

    suspend fun discoverTimestampFor1Min(
        manager: WifiP2pManager, channel: WifiP2pManager.Channel, repeatCount: Int) {
        val discoveryResult = DiscoverService.startServiceDiscovery(serviceRequest!!, manager, channel)
        when(discoveryResult) {
            1 -> { /* Discovery Initiated Successfully */
//                isDiscovering = true
                Log.i(TAG, "WIFI_SD_Testing-ViewModel: Discovery Initiated Successfully")
                /**
                 * resetting the whole discovery after delay
                 */
                delay(10000)
                if(isDiscovering && repeatCount < 6) {
                    discoverTimestampFor1Min(manager, channel, repeatCount+1)
                }
            }
            2 -> { /* Failed to initiate discovery */
                Log.i("TAG", "WIFI_SD_Testing-ViewModel: Failed to initiate discovery")
                _atdStatus.postValue(AttendanceStatusStd.Error("Failed to initiate discovery"))
            }
            3 -> { /* Failed to add Service Request */
                Log.i("TAG", "WIFI_SD_Testing-ViewModel: Failed to add Service Request")
                _atdStatus.postValue(AttendanceStatusStd.Error("Failed to add Service Request"))
            }
            4 -> { /* Failed to clear Service Requests */
                Log.i("TAG", "WIFI_SD_Testing-ViewModel: Failed to clear Service Requests")
                _atdStatus.postValue(AttendanceStatusStd.Error("Failed to clear Service Requests"))
            }
        }
    }

    fun removeServiceRequest(manager: WifiP2pManager, channel: WifiP2pManager.Channel) {
        if(serviceRequest != null) {
            viewModelScope.launch {
                DiscoverService.removeServiceRequest(serviceRequest!!, manager, channel)
            }
        }
    }

    fun martAtd(courseCode: String, timestamp: String) {
        Log.i("TAG", "StdTesting-ViewModel: Calling markAtd")
        viewModelScope.launch {
            val semSec = currUserStd.value?.data?.sem_sec
            val enrollment = currUserStd.value?.data?.enrollment
            if(semSec == null || enrollment == null) {
                Log.i("TAG", "StdTesting-ViewModel: marAtd() Error - null values")
            } else {
                val isContinuingResponse = studentRepository.checkAtdStatus(semSec, courseCode, timestamp)
                when(isContinuingResponse) {
                    "true" -> {
                        // Attendance is still going on
                        val markAtdResult = studentRepository.markAtd(semSec, courseCode, timestamp, enrollment)
                        if(markAtdResult == FirebaseConstants.STATUS_SUCCESSFUL) {
                            _atdStatus.postValue(AttendanceStatusStd.AttendanceMarked())
                        } else {
                            _atdStatus.postValue(AttendanceStatusStd.Error(markAtdResult))
                        }
                    }
                    "false" -> {
                        // Attendance is over
                        _atdStatus.postValue(AttendanceStatusStd.Error("Attendance is Over"))
                    }
                    else -> {
                        _atdStatus.postValue(AttendanceStatusStd.Error(isContinuingResponse))
                    }
                }
            }
        }
    }

    private suspend fun setServiceRequest(
        manager: WifiP2pManager, channel: WifiP2pManager.Channel, serviceInstance: String) {

        //TODO: If it doesn't work, try suspend Coroutine and return true then use await or something like that
        Log.i("TAG", "WIFI_SD_Testing-ViewModel: setServiceRequest() called")
        val txtListener = WifiP2pManager.DnsSdTxtRecordListener { fullDomain, record, device ->
            Log.i(TAG, "WIFI_SD_Testing: txtRecordListener available -$record")
            record[FirebasePaths.TIMESTAMP]?.also {
                serviceList[device.deviceAddress] = it
            }
        }

        val servListener = WifiP2pManager.DnsSdServiceResponseListener { instanceName, registrationType, resourceType ->
            Log.i(TAG, "WIFI_SD_Testing: serviceListener: device - ${resourceType.deviceName}")
            // A service has been discovered. Is this our app?
            if (instanceName.equals(serviceInstance)) {
                // yes it is
                viewModelScope.launch {
                    val isRemoved = DiscoverService.removeServiceRequest(serviceRequest!!, manager, channel)
                    if(isRemoved) {
                        val timestamp = serviceList[resourceType.deviceAddress]
                        _atdStatus.postValue(AttendanceStatusStd.TimestampDiscovered(timestamp!!))
                    } else {
                        Log.i(TAG, "WIFI_SD_Testing: serviceListener: Error in Clearing Service")
                    }

                }
//                lifecycleScope.launch(Dispatchers.IO) {
//                    DiscoverService.removeServiceRequest(serviceRequest!!, manager!!, channel!!)
//                }
            } else {
                Log.i(TAG, "setServiceRequest: not the required service")
            }
        }

        manager.setDnsSdResponseListeners(channel, servListener, txtListener)
        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance()
    }

    fun broadcastTimestamp(manager: WifiP2pManager, channel: WifiP2pManager.Channel, serviceInfo: WifiP2pDnsSdServiceInfo) {

        viewModelScope.launch {
            Log.i("TAG", "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Calling broadcastTimestamp()")
            val broadcastResult = BroadcastService.startServiceBroadcast(serviceInfo, manager, channel)
            when(broadcastResult) {
                1 -> { /* Service Added Successfully */
                    Log.i("TAG", "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Service Added Successfully")
                    broadcastServiceFor20Sec(manager, channel)
                }
                2 -> { /* Failed to add service */
                    Log.i("TAG", "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Failed to add service")
                    _atdStatus.postValue(AttendanceStatusStd.Error("Failed to add service"))
                }
                3 -> { /* Failed to Clear Service */
                    Log.i("TAG", "WIFI_SD_Testing-ViewModel: broadcastTimestamp - Failed to Clear Service")
                    _atdStatus.postValue(AttendanceStatusStd.Error("Failed to Clear Service"))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun broadcastServiceFor20Sec(manager: WifiP2pManager, channel: WifiP2pManager.Channel) {
        // Following is the solution from the post on link - https://stackoverflow.com/questions/26300889/wifi-p2p-service-discovery-works-intermittently
        delay(10000)
        manager.discoverPeers(channel, object: WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i("TAG", "WIFI_SD_Testing-ViewModel: DiscoverPeers - Services Reset Successfully")
            }

            override fun onFailure(reason: Int) {
                Log.i("TAG", "WIFI_SD_Testing-ViewModel: DiscoverPeers - Failed to reset services: Reason-$reason")
            }
        })
        delay(10000)
//        repeat(2) {
//        }
        _atdStatus.postValue(AttendanceStatusStd.BroadcastComplete())
    }

}