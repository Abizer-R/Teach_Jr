package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfCourseDetailsBinding
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfCourseViewModel
import com.example.teachjr.utils.AdapterUtils
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfCourseDetailsFragment : Fragment() {

    private val TAG = ProfCourseDetailsFragment::class.java.simpleName
    private lateinit var binding: FragmentProfCourseDetailsBinding
    private val courseViewModel by viewModels<ProfCourseViewModel>()

    private var courseCode: String? = null
    private var courseName: String? = null
    private var semSec: String? = null
    private var lecCount: Int? = null

//    private var lecDoc: LecturesDocument? = null

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
        courseCode = arguments?.getString(FirebasePaths.COURSE_CODE)
        courseName = arguments?.getString(FirebasePaths.COURSE_NAME)
        semSec = arguments?.getString(FirebasePaths.SEM_SEC)

        if(courseCode == null || courseName == null || semSec == null) {
            Log.i(TAG, "ProfessorTesting_CoursePage: null bundle arguments. courseCode=$courseCode, courseName-$courseName, sem_sec-$semSec")
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        } else {
            binding.tvCourseCode.text = courseCode
            binding.tvCourseName.text = courseName
            binding.tvSection.text = AdapterUtils.getSection(semSec.toString())

            courseViewModel.getLectureCount(semSec!!, courseCode!!)
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
                    lecCount = it.data
                    binding.progressBar.visibility = View.GONE
                    binding.tvLecCount.text = "Total Lectures: ${lecCount.toString()}"
                }
            }
        }

        binding.btnAtdReport.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(FirebasePaths.SEM_SEC, semSec)
            bundle.putString(FirebasePaths.COURSE_CODE, courseCode)
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profAtdReportFragment,bundle)
        }

        binding.fabMarkAtd.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(FirebasePaths.COURSE_CODE, courseCode)
            bundle.putString(FirebasePaths.SEM_SEC, semSec)
            bundle.putInt(FirebasePaths.LEC_COUNT, lecCount!!)
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profMarkAtdFragment, bundle)
        }
    }

}