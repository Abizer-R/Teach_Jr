package com.example.teachjr.ui.professor.profFragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teachjr.R
import com.example.teachjr.data.model.RvProfMarkAtdListItem
import com.example.teachjr.databinding.FragmentProfMarkAtdBinding
import com.example.teachjr.ui.adapters.AttendanceAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfMarkAtdViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.utils.*
import com.example.teachjr.utils.sealedClasses.AttendanceStatusProf
import com.example.teachjr.utils.sealedClasses.Response
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfMarkAtdFragment : Fragment() {

    private val TAG = ProfMarkAtdFragment::class.java.simpleName
    private lateinit var binding: FragmentProfMarkAtdBinding

    private val markAtdViewModel by viewModels<ProfMarkAtdViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    private val attendanceAdapter = AttendanceAdapter()

    private var manager: WifiP2pManager? = null
    private var channel: WifiP2pManager.Channel? = null

    private val SERVICE_TYPE = "_presence._tcp"

    private lateinit var confirmDialog: AlertDialog

    // TODO: Write the flow on paper first, then apply changes


    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if(granted) {
                startAttendance()
//                if(markAtdViewModel.isAtdOngoing) {
//                    endAttendance()
//                } else {
//                    startAttendance()
//                }
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

        showFadeAnimation()
        initialSetup()
        setupViews()
        initiateAtdMarking()
        setupObservers()
    }

    private fun showFadeAnimation() {
        val animation = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        binding.toolbar.startAnimation(animation)
    }

    private fun initialSetup() {

        binding.icClose.setOnClickListener {
            checkExitConditions()
        }

        createConfirmDialog()
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // TODO: Copy the checkExitConditions() and modify if necessary
                checkExitConditions()
//                if(markAtdViewModel.isAtdOngoing) {
//                    confirmDialog.show()
//                } else {
//                    findNavController().navigateUp()
//                }
            }

        })

        binding.fabTryAgain.setOnClickListener {
            initiateAtdMarking()
        }

        binding.fabEndAtd.setOnClickListener {
            endAttendance()
        }
    }

    private fun setupViews() {

        binding.rvAttendance.apply {
            hasFixedSize()
            adapter = attendanceAdapter
            layoutManager = GridLayoutManager(context, 4)
        }




        binding.rvAttendance.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if(dy > 0) {
                    binding.fabEndAtd.hide()
                } else {
                    binding.fabEndAtd.show()
                }
            }
        })
    }

    private fun setupObservers() {

        markAtdViewModel.atdStatus.observe(viewLifecycleOwner) {
            when(it) {
                is AttendanceStatusProf.FetchingTimestamp -> {
                    markAtdViewModel.updateIsAtdOngoing(true)

                    showAtdStatus(Constants.INITIATING_ATTENDANCE)
//                    showTimerLayout(it.remainingTime!!)
                    Log.i(TAG, "WIFI_SD_Observer_Status: Fetching Timestamp")

                }
                is AttendanceStatusProf.Initiated -> {
                    // TODO: Show (present / total) students as well... somewhere....
                    showAtdStatus(Constants.MARKING_ATTENDANCE)

                    /**
                     * Applying LifeCycleScope so that when we exit the fragment,
                     * the flow gets cancelled automatically.
                     * Otherwise, the childEventListener (in Repo) would
                     * keep listening for updates in the lec location
                     */

                    broadcastTimestamp(it.timestamp!!)
                    lifecycleScope.launch {
                        /**
                         * This is a blocking call, therefore it is placed in its different coroutine
                         */
                        markAtdViewModel.observeAtd(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!, it.timestamp)
                    }
                }
                is AttendanceStatusProf.Ended -> {
                    // TODO: Text = "Attendance ended"
                    // TODO: Make a layout which shows the attendance percentage too
                    showSuccessful()
                    markAtdViewModel.updateIsAtdOngoing(false)
                    Toast.makeText(context, "Attendance is over", Toast.LENGTH_SHORT).show()
                }
                is AttendanceStatusProf.Error -> {
                    markAtdViewModel.updateIsAtdOngoing(false)
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                    showError()
                }
            }
        }

        markAtdViewModel.presentList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {}
                is Response.Error -> Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                is Response.Success -> {
                    if(binding.tvNoAtd.visibility == View.VISIBLE) {
                        binding.tvNoAtd.visibility = View.GONE
                        binding.rvAttendance.visibility = View.VISIBLE
                    }

                    val stdList = it.data!!.entries.map { currStd -> RvProfMarkAtdListItem(currStd.key, currStd.value) }
                    attendanceAdapter.updateList(stdList)
                }
            }
        }
    }

    private fun broadcastTimestamp(timestamp: String) {
        val serviceInstance = "${sharedProfViewModel.sem_sec}/${sharedProfViewModel.courseCode}"
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

    private fun initiateAtdMarking() {
        if(Permissions.hasAccessCoarseLocation(activity as Context)
            && Permissions.hasAccessFineLocation(activity as Context)) {

            if(markAtdViewModel.isAtdOngoing == false) {
                startAttendance()
            }

        } else {
            permissionRequestLauncher.launch(Permissions.getPendingPermissions(activity as Activity))
        }
    }

    private fun startAttendance() {
        Log.i(TAG, "ProfessorTesting_MarkAtdPage: startAttendance() called")

        if(sharedProfViewModel.courseValuesNotNull() && sharedProfViewModel.lecCountNotNull()) {
            // TODO: Give an option to choose for manually closing atd or closing in 2 min

            // Makes a new Lec doc and fetches list of enrolled students
            markAtdViewModel.initAtd(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!, sharedProfViewModel.lecCount!!)
        } else {
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()

        }
    }

    private fun endAttendance() {
        val timestamp = markAtdViewModel.atdStatus.value?.timestamp
        if(timestamp != null) {
            markAtdViewModel.endAttendance(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!, timestamp)
            lifecycleScope.launch {
                markAtdViewModel.stopBroadcasting(manager!!, channel!!)
            }
            sharedProfViewModel.updateLecCount(sharedProfViewModel.lecCount!! + 1)
        } else {
            Log.i(TAG, "ProfessorTesting_MarkAtdPage: null timestamp")
            Toast.makeText(context, "null timestamp", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        if(channel != null) {
            manager?.clearLocalServices(channel, null)
        }
    }

    private fun checkExitConditions() {
        if(markAtdViewModel.isAtdOngoing) {
            confirmDialog.show()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun createConfirmDialog() {
        confirmDialog = AlertDialog.Builder(context)
            .setTitle(Constants.ALERT_DIALOG_END_ATTENDANCE_PRIMARY)
            .setMessage(Constants.ALERT_DIALOG_END_ATTENDANCE_SECONDARY)
            .setPositiveButton(Constants.YES) { _, _ ->
                endAttendance()
                findNavController().navigateUp()
            }
            .setNegativeButton(Constants.NO, null)
            .create()
    }

    private fun showAtdStatus(atdString: String) {
        binding.cvErrorLayout.visibility = View.GONE

        binding.cvAtdStatusLayout.visibility = View.VISIBLE
        binding.cvRVLayout.visibility = View.VISIBLE
        binding.fabEndAtd.visibility = View.VISIBLE
        if(atdString.equals(Constants.MARKING_ATTENDANCE)) {
            binding.loadingAnimation.visibility = View.VISIBLE
            binding.loadingAnimation.playAnimation()
        } else {
            binding.loadingAnimation.visibility = View.GONE
        }

        binding.tvAtdStatus.text = atdString

//        binding.tvNoAtd.visibility = View.VISIBLE
//        binding.rvAttendance.visibility = View.GONE

    }

    private fun showError() {
        binding.cvAtdStatusLayout.visibility = View.GONE
        binding.cvRVLayout.visibility = View.GONE
        binding.fabEndAtd.visibility = View.GONE

        binding.cvErrorLayout.visibility = View.VISIBLE
        binding.loadingAnimation.cancelAnimation()
    }


    private fun showSuccessful() {
        binding.cvAtdStatusLayout.visibility = View.GONE
        binding.fabEndAtd.visibility = View.GONE
        binding.loadingAnimation.cancelAnimation()


        binding.cvErrorLayout.visibility = View.VISIBLE
        binding.icError.setImageResource(R.drawable.ic_baseline_verified_64)
        binding.tvErrorMessage.text = Constants.ATTENDANCE_MARKED_SUCCESSFULLY
        binding.tvSolutionMsg.text = Constants.EXIT_SCREEN_ALLOWED_PROMPT
        binding.fabTryAgain.visibility = View.GONE

        // Setting these invisible because I need a margin below the "exit now" TextView
        binding.tvSolution1.visibility = View.INVISIBLE
        binding.tvSolution2.visibility = View.INVISIBLE
    }

}