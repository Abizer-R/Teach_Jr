package com.example.teachjr.ui.professor.profFragments

import android.R.attr.path
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfAtdReportBinding
import com.example.teachjr.ui.adapters.AtdReportViewPagerAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfAtdReportViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.utils.Constants
import com.example.teachjr.utils.Permissions
import com.example.teachjr.utils.sealedClasses.Response
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class ProfAtdReportFragment : Fragment() {

    private val TAG = ProfAtdReportFragment::class.java.simpleName
    private lateinit var binding: FragmentProfAtdReportBinding

    private lateinit var downloadFolderFile: File

    private val atdReportViewModel by activityViewModels<ProfAtdReportViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    private lateinit var viewPagerAdapter: AtdReportViewPagerAdapter
    private val tabLayoutTitles = arrayListOf("Lectures", "Students")

    private lateinit var confirmDialog: AlertDialog

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if(granted) {
                Toast.makeText(context, "GRANTED", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "not GRANTED", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfAtdReportBinding.inflate(layoutInflater)

        downloadFolderFile = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            //Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_white_32)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        setupOptionsMenu()
        createConfirmDownloadDialog()
        setupObservers()


        viewPagerAdapter = AtdReportViewPagerAdapter(this)
//        binding.viewPager.offscreenPageLimit = 2 // This ensures that both are fragments stay alive all the time
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabLayoutTitles[position]
        }.attach()

        if(sharedProfViewModel.courseValuesNotNull()) {
            lifecycleScope.launch {
                atdReportViewModel.getAtdDetails(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!)
            }
        } else {
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupOptionsMenu() {
        binding.toolbar.inflateMenu(R.menu.atd_report_page_menu)
        binding.toolbar.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_download_report -> {
                    if(atdReportViewModel.detailsLoaded) {
                        checkPermissionAndShowDialog()
                    } else {
                        Toast.makeText(context, "Data not available", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun setupObservers() {

        atdReportViewModel.atdReportSaveStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> { showSaving() }
                is Response.Error -> {
                    stopSaving()
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    stopSaving()
                    Toast.makeText(context, "Downloaded at ${it.data}", Toast.LENGTH_SHORT).show()
                    // TODO: Alert dialog - Upload to google drive | open in folder | cancel
                }
            }
        }
    }

    private fun checkPermissionAndShowDialog() {
        if(Permissions.hasReadStoragePermissions(activity as Context)
            && Permissions.hasWriteStoragePermissions(activity as Context)) {

            confirmDialog.show()
        } else {
            permissionRequestLauncher.launch(Permissions.getPendingStoragePermissions(activity as Activity))
        }
    }

    private fun createConfirmDownloadDialog() {
        confirmDialog = AlertDialog.Builder(context)
            .setTitle("Convert to excel?")
            .setMessage("Attendance report will be saved as excel sheet")
            .setPositiveButton(Constants.YES) { _, _ ->

                lifecycleScope.launch(Dispatchers.IO) {
                    atdReportViewModel.downloadExcelSheet(downloadFolderFile.path.toString())
                }
            }
            .setNegativeButton(Constants.NO, null)
            .create()
    }

    private fun showSaving() {
        binding.viewPager.alpha = 0.5F
        binding.cvSaving.visibility = View.VISIBLE
    }

    private fun stopSaving() {
        binding.viewPager.alpha = 1F
        binding.cvSaving.visibility = View.GONE
    }
}