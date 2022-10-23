package com.example.teachjr.ui.professor.profFragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfMarkAtdBinding
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.utils.Permissions


class ProfMarkAtdFragment : Fragment() {

    private val TAG = ProfMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentProfMarkAtdBinding
    private val profViewModel by activityViewModels<ProfViewModel>()

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if(granted) {
                startAttendance()
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
        setupViews()
    }

    private fun setupViews() {

        binding.rvAttendance.apply {
            // TODO: Setup RecyclerView
        }

        binding.fabAttendance.setOnClickListener {
            if(Permissions.hasAccessCoarseLocation(activity as Context)
                && Permissions.hasAccessFineLocation(activity as Context)) {
                startAttendance()
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

    private fun startAttendance() {
        Log.i(TAG, "ProfessorTesting_CoursePage: startAttendance() called")

        // TODO: Check if location and wifi is on

        // TODO Make viewmodel and repo functions
        // TODO-1: Create a unique otp
        // TODO-2: Create a new lecFile in database
        // TODO-3: Broadcast the otp
    }

}