package com.example.teachjr.ui.viewmodels.studentViewModels

import android.annotation.SuppressLint
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachjr.data.source.repository.StudentRepository
import com.example.teachjr.utils.AttendanceStatusStd
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.WifiSD.BroadcastService
import com.example.teachjr.utils.WifiSD.DiscoverService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StdMarkAtdViewModel
    @Inject constructor(
        private val studentRepository: StudentRepository
    ): ViewModel() {

    private val TAG = StdMarkAtdViewModel::class.java.simpleName

    private val serviceList = mutableMapOf<String, String>()
    private var serviceRequest: WifiP2pDnsSdServiceRequest? = null

    private var _isDiscovering = false
    val isDiscovering: Boolean
        get() = _isDiscovering
    fun updateIsDiscovering(newState: Boolean) {_isDiscovering = newState}

    private val _atdStatus = MutableLiveData<AttendanceStatusStd>()
    val atdStatus: LiveData<AttendanceStatusStd>
        get() = _atdStatus

    suspend fun discoverTimestamp(
        manager: WifiP2pManager, channel: WifiP2pManager.Channel, serviceInstance: String) {

        Log.i("TAG", "WIFI_SD_Testing-ViewModel: discoverTimestamp() Called")
        _atdStatus.postValue(AttendanceStatusStd.DiscoveringTimestamp())
        setServiceRequest(manager, channel, serviceInstance)
        discoverTimestampFor1Min(manager, channel, 0)
    }

    suspend fun discoverTimestampFor1Min(
        manager: WifiP2pManager, channel: WifiP2pManager.Channel, repeatCount: Int) {
        val discoveryResult = DiscoverService.startServiceDiscovery(serviceRequest!!, manager, channel)
        when(discoveryResult) {
            1 -> { /* Discovery Initiated Successfully */
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
                _isDiscovering = false
            }
        }
    }

    fun martAtd(courseCode: String, timestamp: String, sem_sem: String, enrollment: String) {
        Log.i("TAG", "StdTesting-ViewModel: Calling markAtd")
        viewModelScope.launch {
            val isContinuingResponse = studentRepository.checkAtdStatus(sem_sem, courseCode, timestamp)
            when(isContinuingResponse) {
                "true" -> {
                    // Attendance is still going on
                    val markAtdResult = studentRepository.markAtd(sem_sem, courseCode, timestamp, enrollment)
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

    private suspend fun setServiceRequest(
        manager: WifiP2pManager, channel: WifiP2pManager.Channel, serviceInstance: String) {
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