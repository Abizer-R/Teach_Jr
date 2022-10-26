package com.example.teachjr.ui.professor.profFragments

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
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfMarkAtdBinding
import com.example.teachjr.ui.adapters.AttendanceAdapter
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Permissions
import com.example.teachjr.utils.Response
import dagger.hilt.android.internal.managers.FragmentComponentManager.initializeArguments


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
                checkGpsAndStartAttendance()
            } else {
                permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
            }


//            val dummyList = arrayListOf<String>(
//                "0818CS201001", "0818CS201002", "0818CS201003", "0818CS201004", "0818CS201005"
//            )
//            attendanceAdapter.updateList(dummyList)
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
        profViewModel.presentList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {} // TODO: Indicate loading
                is Response.Error -> Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                is Response.Success -> {
                    attendanceAdapter.updateList(it.data!!)
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
        Log.i(TAG, "ProfessorTesting_MarkAtdPage: startAttendance() called")

        // TODO: Check if location and wifi is on
        if(courseCode == null || semSec == null || lecCount == null) {
            Log.i(TAG, "ProfessorTesting_MarkAtdPage: null bundle arguments")
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        } else {
            // Makes a new Lec doc and fetches list of enrolled students
            val stdList = profViewModel.initAtd(semSec!!, courseCode!!, lecCount!!)
        }
    }

}