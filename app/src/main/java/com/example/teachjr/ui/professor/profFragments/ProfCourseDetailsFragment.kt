package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfCourseDetailsBinding
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfCourseViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.utils.AdapterUtils
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfCourseDetailsFragment : Fragment() {

    private val TAG = ProfCourseDetailsFragment::class.java.simpleName
    private lateinit var binding: FragmentProfCourseDetailsBinding

    private val courseViewModel by viewModels<ProfCourseViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfCourseDetailsBinding.inflate(layoutInflater)
        Log.i(TAG, "ProfessorTesting_CoursePage: Professor CoursePage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            title = "Course Detail"

            //Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        initialSetup()
    }

    private fun initialSetup() {

        if(sharedProfViewModel.courseValuesNotNull()) {
            binding.tvCourseCode.text = sharedProfViewModel.courseCode
            binding.tvCourseName.text = sharedProfViewModel.courseName
            binding.tvSection.text = AdapterUtils.getSection(sharedProfViewModel.sem_sec.toString())

            courseViewModel.getLectureCount(sharedProfViewModel.sem_sec!!, sharedProfViewModel.courseCode!!)

        } else {
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        }

        courseViewModel.lecCount.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    Log.i(TAG, "ProfessorTesting_CoursePage: lecCount_Error - ${it.errorMessage}")
                    Toast.makeText(context, "Error: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "ProfessorTesting_CoursePage: lecCount - ${it.data}")
//                    lecCount = it.data
                    sharedProfViewModel.updateLecCount(it.data!!)
                    binding.progressBar.visibility = View.GONE
                    binding.tvLecCount.text = "Total Lectures: ${it.data.toString()}"
                }
            }
        }

        binding.btnAtdReport.setOnClickListener {
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profAtdReportFragment)
        }

        binding.fabMarkAtd.setOnClickListener {
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profMarkAtdFragment)
        }
    }

}