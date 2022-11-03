package com.example.teachjr.ui.student.stdFragments

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdMarkAtdBinding
import com.example.teachjr.ui.professor.profFragments.ProfMarkAtdFragment
import com.example.teachjr.ui.viewmodels.StudentViewModel
import com.example.teachjr.utils.AttendanceStatusStd
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Permissions
import com.example.teachjr.utils.Response
import com.example.teachjr.utils.WifiSD.DiscoverService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.processNextEventInCurrentThread

class StdMarkAtdFragment : Fragment() {

    private val TAG = StdMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentStdMarkAtdBinding
    private val stdViewModel by activityViewModels<StudentViewModel>()

    private var courseCode: String? = null
    private var sem_sec: String? = null

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    private val SERVICE_TYPE = "_presence._tcp"
//    private var isDiscovering = false

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if(granted) {
                checkGpsAndStartAttendance()
            } else {
                // TODO: Display a message that attendance cannot be initiated without permission
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStdMarkAtdBinding.inflate(layoutInflater)
        Log.i(TAG, "StudentTesting_MarkAtdPage: Student MarkAtdPage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initArguments()
        setupViews()
        setupObservers()
    }

    private fun initArguments() {
        courseCode = arguments?.getString(FirebasePaths.COURSE_CODE)
    }

    private fun setupViews() {
        binding.fabMarkAtd.setOnClickListener {
            if(Permissions.hasAccessCoarseLocation(activity as Context)
                && Permissions.hasAccessFineLocation(activity as Context)) {

                if(stdViewModel.isDiscovering == false) {
                    checkGpsAndStartAttendance()
                } else {
                    stdViewModel.removeServiceRequest(manager!!, channel!!)
                    binding.fabMarkAtd.apply {
                        extend()
                        setIconResource(R.drawable.ic_baseline_add_24)
                        backgroundTintList = ColorStateList.valueOf(Color.parseColor("#03dac5"))
                    }
                    stdViewModel.updateIsDiscovering(false)
                    binding.progressBar.visibility = View.GONE
                }

            } else {
                permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
            }
        }
    }

    private fun setupObservers() {
        stdViewModel.atdStatus.observe(viewLifecycleOwner) {
            when(it) {
//                is AttendanceStatusStd.InitiatingDiscovery -> binding.progressBar.visibility = View.VISIBLE
                is AttendanceStatusStd.DiscoveringTimestamp -> {
                    stdViewModel.updateIsDiscovering(true)
                    binding.progressBar.visibility = View.VISIBLE
                    binding.fabMarkAtd.apply {
                        shrink()
                        setIconResource(R.drawable.ic_baseline_close_24)
                        backgroundTintList = ColorStateList.valueOf(Color.parseColor("#f7292b"))
                    }
                    /**
                     * Discovering Timestamp
                     */
                    binding.tvStatus.text = "Initial Loading... Please Wait"
                    // TODO: Implement an "You haven't marked your attendance yet, do you still want to exit?" pop up
                }
                is AttendanceStatusStd.TimestampDiscovered -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvTimeStamp.text = it.timestamp

                    binding.fabMarkAtd.visibility = View.GONE
                    binding.tvStatus.text = "Marking Attendance... Please Wait"
                    stdViewModel.updateIsDiscovering(false)

                    /**
                     * Marking Attendance
                     */
                    stdViewModel.martAtd(courseCode!!, it.timestamp.toString())
                    // TODO: Implement an "You haven't marked your attendance yet, do you still want to exit?" pop up
                }
                is AttendanceStatusStd.AttendanceMarked -> {
//                    binding.fabMarkAtd.visibility = View.GONE
                    binding.tvStatus.text = "Attendance Marked. Broadcasting code... Please Wait"

                    /**
                     * Broadcasting Timestamp
                     */
                    broadcastTimestamp(it.timestamp.toString())
                    // TODO: Implement an "You need to wait xx sec before exiting" pop up
                }
                is AttendanceStatusStd.BroadcastComplete -> {
                    binding.tvStatus.text = "Thank you for waiting, you can exit the screen now."

                }
                is AttendanceStatusStd.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                    binding.tvStatus.text = it.errorMessage.toString()
                }
            }
        }
    }

    private fun checkGpsAndStartAttendance(){
        val mLocationManager = (activity as Context).getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Checking GPS is enabled
        val mGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if(mGPS) {
            startAttendance()
        } else {
            // TODO: Display a popup
            Toast.makeText(context, "Please turn on GPS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAttendance() {
        Log.i(TAG, "StudentTesting_MarkAtdPage: startAttendance() called")

        // TODO: Check if location and wifi is on
        if(courseCode == null) {
            Log.i(TAG, "StudentTesting_MarkAtdPage: null bundle arguments")
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        } else {

            // TODO: Start Discovery
            discoverTimestamp()

        }
    }

    private fun discoverTimestamp() {
        sem_sec = stdViewModel.currUserStd.value?.data?.sem_sec
        if(sem_sec == null) {
            Log.i(TAG, "StudentTesting_MarkAtdPage: sem_sec is null")
            Toast.makeText(context, "sem_sec is null", Toast.LENGTH_SHORT).show()
        }
        if(courseCode == null) {
            Log.i(TAG, "StudentTesting_MarkAtdPage: courseCode is null")
            Toast.makeText(context, "courseCode is null", Toast.LENGTH_SHORT).show()
        }

        if(manager == null) {
            val applicationContext = (activity as Activity).applicationContext
            manager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        }
        if(channel == null) {
            channel = manager?.initialize(context, Looper.getMainLooper(), null)
        }

        if(sem_sec != null && courseCode != null) {
            val serviceInstance = "$sem_sec/$courseCode"
            lifecycleScope.launch {

                stdViewModel.discoverTimestamp(manager!!, channel!!, serviceInstance)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(channel != null) {
            manager?.clearLocalServices(channel, null)
            stdViewModel.removeServiceRequest(manager!!, channel!!)
        }
    }

    private fun broadcastTimestamp(timestamp: String) {
        val serviceInstance = "$sem_sec/$courseCode"
        val record = mapOf( FirebasePaths.TIMESTAMP to timestamp )
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceInstance, SERVICE_TYPE, record)
        if(manager == null) {
            val applicationContext = (activity as Activity).applicationContext
            manager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        }
        if(channel == null) {
            channel = manager?.initialize(context, Looper.getMainLooper(), null)
        }
        stdViewModel.broadcastTimestamp(manager!!, channel!!, serviceInfo)
    }

}