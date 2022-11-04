package com.example.teachjr.ui.student.stdFragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdMarkAtdBinding
import com.example.teachjr.ui.viewmodels.studentViewModels.StdMarkAtdViewModel
import com.example.teachjr.utils.AttendanceStatusStd
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StdMarkAtdFragment : Fragment() {

    private val TAG = StdMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentStdMarkAtdBinding
    private val markAtdViewModel by viewModels<StdMarkAtdViewModel>()

    private var courseCode: String? = null
    private var sem_sec: String? = null
    private var enrollment: String? = null

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    private val SERVICE_TYPE = "_presence._tcp"

    private lateinit var confirmDialog: AlertDialog

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
        initialSetup()
        setupViews()
        setupObservers()
    }

    private fun initialSetup() {
        courseCode = arguments?.getString(FirebasePaths.COURSE_CODE)
        sem_sec = arguments?.getString(FirebasePaths.SEM_SEC)
        enrollment = arguments?.getString(FirebasePaths.STUDENT_ENROLLMENT)

        createConfirmDialog()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when(markAtdViewModel.atdStatus.value) {
                    is AttendanceStatusStd.DiscoveringTimestamp -> confirmDialog.show()
                    is AttendanceStatusStd.TimestampDiscovered -> confirmDialog.show()
                    is AttendanceStatusStd.AttendanceMarked -> {
                        Toast.makeText(context, "You cannot exit right now. Please Wait a few seconds", Toast.LENGTH_SHORT).show()
                    }
                    else -> findNavController().navigateUp()
                }
            }

        })
    }

    private fun setupViews() {
        binding.fabMarkAtd.setOnClickListener {
            if(Permissions.hasAccessCoarseLocation(activity as Context)
                && Permissions.hasAccessFineLocation(activity as Context)) {

                if(markAtdViewModel.isDiscovering == false) {
                    checkGpsAndStartAttendance()
                } else {
                    if(manager != null && channel != null) {
                        markAtdViewModel.removeServiceRequest(manager!!, channel!!)
                    }
                    binding.fabMarkAtd.apply {
                        extend()
                        setIconResource(R.drawable.ic_baseline_add_24)
                        backgroundTintList = ColorStateList.valueOf(Color.parseColor("#03dac5"))
                    }
                    markAtdViewModel.updateIsDiscovering(false)
                    binding.progressBar.visibility = View.GONE
                }

            } else {
                permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
            }
        }
    }

    private fun setupObservers() {
        markAtdViewModel.atdStatus.observe(viewLifecycleOwner) {
            when(it) {
//                is AttendanceStatusStd.InitiatingDiscovery -> binding.progressBar.visibility = View.VISIBLE
                is AttendanceStatusStd.DiscoveringTimestamp -> {
                    markAtdViewModel.updateIsDiscovering(true)
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
                }
                is AttendanceStatusStd.TimestampDiscovered -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvTimeStamp.text = it.timestamp

                    binding.fabMarkAtd.visibility = View.GONE
                    binding.tvStatus.text = "Marking Attendance... Please Wait"
                    markAtdViewModel.updateIsDiscovering(false)

                    /**
                     * Marking Attendance
                     */
                    markAtdViewModel.martAtd(courseCode!!, it.timestamp.toString(), sem_sec!!, enrollment!!)
                }
                is AttendanceStatusStd.AttendanceMarked -> {
//                    binding.fabMarkAtd.visibility = View.GONE
                    binding.tvStatus.text = "Attendance Marked. Broadcasting code... Please Wait"

                    /**
                     * Broadcasting Timestamp
                     */
                    broadcastTimestamp(it.timestamp.toString())
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


        // TODO: Check if WIFI is on
        if(mGPS) {
            startAttendance()
        } else {
            Toast.makeText(context, "Please turn on GPS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startAttendance() {
        Log.i(TAG, "StudentTesting_MarkAtdPage: startAttendance() called")

        if(courseCode == null || sem_sec == null || enrollment == null) {
            Log.i(TAG, "StudentTesting_MarkAtdPage: null arguments, $courseCode, $sem_sec, $enrollment")
            Toast.makeText(context, "Couldn't fetch all arguments, Some might be null", Toast.LENGTH_SHORT).show()
        } else {
            if(manager == null) {
                val applicationContext = (activity as Activity).applicationContext
                manager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
            }
            if(channel == null) {
                channel = manager?.initialize(context, Looper.getMainLooper(), null)
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val serviceInstance = "$sem_sec/$courseCode"
                markAtdViewModel.discoverTimestamp(manager!!, channel!!, serviceInstance)
//                discoverTimestamp()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if(channel != null) {
            manager?.clearLocalServices(channel, null)
            markAtdViewModel.removeServiceRequest(manager!!, channel!!)
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
        markAtdViewModel.broadcastTimestamp(manager!!, channel!!, serviceInfo)
    }

    private fun createConfirmDialog() {
        confirmDialog = AlertDialog.Builder(context)
            .setTitle("Cancel Attendance?")
            .setMessage("Your attendance hasn't been marked yet")
            .setPositiveButton("Yes") { _, _ ->
                markAtdViewModel.removeServiceRequest(manager!!, channel!!)
                findNavController().navigateUp()
            }
            .setNegativeButton("No", null)
            .create()
    }

}