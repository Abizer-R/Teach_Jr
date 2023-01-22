package com.example.teachjr.ui.professor.profFragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
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

    private lateinit var confirmDialogDownload: AlertDialog
    private lateinit var confirmDialogOpenUpload: AlertDialog

    private val permissionRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if(granted) {
                openExcelFile()
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
//        downloadFolderFile = requireContext().filesDir

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
        createConfirmDialogOpenUpload()
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
                    confirmDialogOpenUpload.show()
                    binding.layoutFileLocation.visibility = View.VISIBLE
                    binding.tvFileLocation.text = downloadFolderFile.path.toString()
                }
            }
        }
    }

    private fun checkPermissionAndShowDialog() {
        if(Permissions.hasReadStoragePermissions(activity as Context)
            && Permissions.hasWriteStoragePermissions(activity as Context)) {

            confirmDialogDownload.show()
        } else {
            permissionRequestLauncher.launch(Permissions.getPendingStoragePermissions(activity as Activity))
        }
    }

    private fun createConfirmDownloadDialog() {
        confirmDialogDownload = AlertDialog.Builder(context)
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

    private fun createConfirmDialogOpenUpload() {
        confirmDialogOpenUpload = AlertDialog.Builder(context)
            .setTitle("File Saved")
//            .setMessage("Do you want to open it or upload it to google drive?")
            .setMessage("Do you want to open it?")
            .setPositiveButton("Open") { _, _ ->

                openExcelFile()
            }
//            .setNegativeButton("Upload") {_,_ -> }
//            .setNeutralButton("Cancel", null)
            .setNegativeButton("Cancel", null)
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

    private fun openExcelFile() {
        try {
            if(atdReportViewModel.sheetName != null) {
                val filePath = File(downloadFolderFile, "")
                val sheet = File(filePath, atdReportViewModel.sheetName!!)
                val contentUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.teachjr.fileProvider",
                    sheet
                )
                Log.i(TAG, "TESTING, URI: ${contentUri.toString()}")

                val intent = Intent(Intent.ACTION_VIEW)
                if(contentUri.toString().contains(".xls") || contentUri.toString().contains(".xlsx")) {
                    intent.setDataAndType(contentUri, "application/vnd.ms-excel")
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

//                        val file = File(atdReportViewModel.filePath)
//                        OpenExcel.openFile(requireContext(), file)
            }
        } catch (e: ActivityNotFoundException) {
//                    binding.tvNoActivityToHandleIntent.text = binding.tvNoActivityToHandleIntent.text.toString() + downloadFolderFile.path.toString()
//                    binding.tvNoActivityToHandleIntent.visibility = View.VISIBLE
            Toast.makeText(context, "You don't have any app that can open an EXCEL file", Toast.LENGTH_LONG).show()
        }
        catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }
}