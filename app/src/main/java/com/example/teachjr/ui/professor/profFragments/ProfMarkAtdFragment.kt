package com.example.teachjr.ui.professor.profFragments

import android.app.Activity
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.switchMap
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfMarkAtdBinding
import com.example.teachjr.ui.adapters.AttendanceAdapter
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.utils.AttendanceStatus
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Permissions
import com.example.teachjr.utils.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfMarkAtdFragment : Fragment() {

    private val TAG = ProfMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentProfMarkAtdBinding
    private val profViewModel by activityViewModels<ProfViewModel>()

    private val attendanceAdapter = AttendanceAdapter()

    private var courseCode: String? = null
    private var semSec: String? = null
    private var lecCount: Int? = null

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    private val SERVICE_TYPE = "_presence._tcp"

    private var isAtdOngoing: Boolean = false


    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if(granted) {
                if(isAtdOngoing) {
                    endAttendance()
                } else {
                    checkGpsAndStartAttendance()
                }
            } else {
                // TODO: Display a message that attendance cannot be initiated without permission
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfMarkAtdBinding.inflate(layoutInflater)
        Log.i(TAG, "ProfessorTesting_MarkAtdPage: Professor MarkAtdPage Created")
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
        semSec = arguments?.getString(FirebasePaths.SEM_SEC)
        lecCount = arguments?.getInt(FirebasePaths.LEC_COUNT)
    }

    private fun setupViews() {

        binding.rvAttendance.apply {
            hasFixedSize()
            adapter = attendanceAdapter
            layoutManager = GridLayoutManager(context, 4)
        }

        binding.fabAttendance.setOnClickListener {
            if(Permissions.hasAccessCoarseLocation(activity as Context)
                && Permissions.hasAccessFineLocation(activity as Context)) {

                if(isAtdOngoing) {
                    endAttendance()
                } else {
                    checkGpsAndStartAttendance()
                }
            } else {
                permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
            }
        }

        binding.rvAttendance.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dy > 0) {
                    binding.fabAttendance.hide()
                } else {
                    binding.fabAttendance.show()
                }
            }
        })
    }

    private fun setupObservers() {

        profViewModel.atdStatus.observe(viewLifecycleOwner) {
            when(it) {
                is AttendanceStatus.FetchingTimestamp -> {
                    isAtdOngoing = true
                    binding.progressBar.visibility = View.VISIBLE
                    updateFAB(isEnabled = false)
                }
                is AttendanceStatus.Initiated -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvTimeStamp.text = it.timestamp
                    updateFAB(atdOngoing = true)

                    /**
                     * Applying LifeCycleScope so that when we exit the fragment,
                     * the flow gets cancelled automatically.
                     * Otherwise, the childEventListener (in Repo) would
                     * keep listening for updates in the lec location
                     */

                    broadcastTimestamp(it.timestamp!!)

                    // TODO: REVIEW THE CODE BELOW
//                    val serviceInstance = "$semSec/$courseCode"
//                    val record = mapOf( FirebasePaths.TIMESTAMP to it.timestamp )
//                    val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceInstance, SERVICE_TYPE, record)
//                    if(manager == null) {
//                        val applicationContext = (activity as Activity).applicationContext
//                        manager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
//                    }
//                    if(channel == null) {
//                        channel = manager?.initialize(context, Looper.getMainLooper(), null)
//                    }
//
//                    profViewModel.broadcastTimestamp(manager!!, channel!!, serviceInfo)
                    lifecycleScope.launch {
                        /**
                         * This is a blocking call, therefore it is placed in its different coroutine
                         */
                        profViewModel.observeAtd(semSec!!, courseCode!!, it.timestamp!!)
                    }
                }
                is AttendanceStatus.Ended -> {
                    updateFAB(atdEnded = true)
                    isAtdOngoing = false
                    Toast.makeText(context, "Attendance is over", Toast.LENGTH_SHORT).show()
                }
                is AttendanceStatus.Error -> {
                    // TODO: Give some functionality to retry
                    isAtdOngoing = false
                    updateFAB(atdEnded = true)
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        profViewModel.presentList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {} // TODO: Indicate "Marking Attendance"
                is Response.Error -> Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                is Response.Success -> {
                    attendanceAdapter.updateList(it.data!!)
                }
            }
        }
    }

    private fun broadcastTimestamp(timestamp: String) {
        val serviceInstance = "$semSec/$courseCode"
        val record = mapOf( FirebasePaths.TIMESTAMP to timestamp )
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceInstance, SERVICE_TYPE, record)
        if(manager == null) {
            val applicationContext = (activity as Activity).applicationContext
            manager = applicationContext.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        }
        if(channel == null) {
            channel = manager?.initialize(context, Looper.getMainLooper(), null)
        }
        profViewModel.broadcastTimestamp(manager!!, channel!!, serviceInfo)
    }

//    suspend fun startBroadcasting(timestamp: String) {
//        val serviceInstance = "$semSec/$courseCode"
//        val record = mapOf( FirebasePaths.TIMESTAMP to timestamp )
//        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(serviceInstance, SERVICE_TYPE, record)
//        profViewModel.broadcastTimestamp(manager!!, channel!!, serviceInfo)
//    }

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
        Log.i(TAG, "ProfessorTesting_MarkAtdPage: startAttendance() called")

        // TODO: Check if location and wifi is on
        if(courseCode == null || semSec == null || lecCount == null) {
            Log.i(TAG, "ProfessorTesting_MarkAtdPage: null bundle arguments")
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        } else {
            // Makes a new Lec doc and fetches list of enrolled students
            profViewModel.initAtd(semSec!!, courseCode!!, lecCount!!)
        }
    }

    private fun endAttendance() {
        val timestamp = profViewModel.atdStatus.value?.timestamp
        if(timestamp != null) {
            profViewModel.endAttendance(semSec!!, courseCode!!, timestamp)
            lifecycleScope.launch {
                profViewModel.stopBroadcasting(manager!!, channel!!)
            }
        } else {
            Log.i(TAG, "ProfessorTesting_MarkAtdPage: null timestamp")
            Toast.makeText(context, "null timestamp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFAB(isEnabled: Boolean = true, atdOngoing: Boolean = false, atdEnded: Boolean = false) {

        if(isEnabled) {
            binding.fabAttendance.isEnabled = true
            binding.fabAttendance.isClickable = true
        } else {
            binding.fabAttendance.isEnabled = false
            binding.fabAttendance.isClickable = false
        }

        if(atdOngoing) {
            binding.fabAttendance.apply {
                shrink()
                setIconResource(R.drawable.ic_baseline_close_24)
                backgroundTintList = ColorStateList.valueOf(Color.parseColor("#f7292b"))
            }
        }

        if(atdEnded) {
            binding.fabAttendance.visibility = View.GONE
        }
    }

}