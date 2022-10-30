package com.example.teachjr.ui.student.stdFragments

import android.app.Activity
import android.content.Context
import android.location.LocationManager
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdMarkAtdBinding
import com.example.teachjr.ui.professor.profFragments.ProfMarkAtdFragment
import com.example.teachjr.ui.viewmodels.StudentViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Permissions
import com.example.teachjr.utils.Response
import kotlinx.coroutines.processNextEventInCurrentThread

class StdMarkAtdFragment : Fragment() {

    private val TAG = StdMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentStdMarkAtdBinding
    private val stdViewModel by activityViewModels<StudentViewModel>()

    private var courseCode: String? = null

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

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
                checkGpsAndStartAttendance()
            } else {
                permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
            }
        }
    }

    private fun setupObservers() {
        stdViewModel.markAtdStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                    binding.tvStatus.text = it.errorMessage.toString()
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.fabMarkAtd.apply {
                        isEnabled = false
                        isClickable = false
                    }
                    // TODO: Broadcast the code for 30 sec
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
            checkEditTextAndMarkAtd()

        }
    }

    private fun checkEditTextAndMarkAtd() {
        val timeStamp = binding.etTimeStamp.text.toString()
        if(timeStamp.isBlank()) {
            Toast.makeText(context, "Please enter timeStamp", Toast.LENGTH_SHORT).show()
            return
        } else {
            stdViewModel.martAtd(courseCode!!, timeStamp)
        }
    }

}