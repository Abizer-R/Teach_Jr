package com.example.teachjr.ui.student.stdFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdCourseDetailsBinding
import com.example.teachjr.ui.adapters.StdLecListAdapter
import com.example.teachjr.ui.viewmodels.studentViewModels.StdCourseViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StdCourseDetailsFragment : Fragment() {

    private val TAG = StdCourseDetailsFragment::class.java.simpleName
    private lateinit var binding: FragmentStdCourseDetailsBinding
    private val courseViewModel by viewModels<StdCourseViewModel>()

    private var courseCode: String? = null
    private var courseName: String? = null
    private var profName: String? = null
    private var sem_sec: String? = null

    private val stdLecListAdapter = StdLecListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStdCourseDetailsBinding.inflate(layoutInflater)
        Log.i(TAG, "StudentTesting_CoursePage: Student CoursePage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.apply {
            title = "Course Detail"

            // Adding up navigation
            setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)
            setNavigationOnClickListener {
            }
        }


        initialSetup()
        setupViews()
    }

    private fun initialSetup() {
        courseCode = arguments?.getString(FirebasePaths.COURSE_CODE)
        courseName = arguments?.getString(FirebasePaths.COURSE_NAME)
        profName = arguments?.getString(FirebasePaths.COURSE_PROF_NAME)
        sem_sec = arguments?.getString(FirebasePaths.SEM_SEC)

        if(courseCode == null || courseName == null || profName == null || sem_sec == null) {
            Log.i(TAG, "StudentTesting_CoursePage: null bundle arguments")
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        } else {
            binding.tvCourseCode.text = courseCode
            binding.tvCourseName.text = courseName
            binding.tvProfName.text = profName

            courseViewModel.getAttendanceDetails(courseCode, sem_sec)
        }

        courseViewModel.atdDetails.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Log.i(TAG, "StudentTesting_CoursePage: lecDetails_Error - ${it.errorMessage}")
                    Toast.makeText(context, "Error: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Log.i(TAG, "StudentTesting_CoursePage: lecDetails - ${it.data}")
                    binding.tvTotalLecCount.text = it.data!!.totalLecCount.toString()
                    binding.tvLecAttendedCount.text = it.data.attendedLecCount.toString()
                    binding.tvLecMissedCount.text = it.data.missedLecCount.toString()

                    val revLecList = it.data.lecList.reversed()

                    // Show mark Attendance if attendance is onGoing
                    if(revLecList[0].isContinuing) {
                        binding.fabMarkAtd.visibility = View.VISIBLE
                    }
                    stdLecListAdapter.updateList(revLecList)


                }
            }
        }
    }

    private fun setupViews() {
        binding.rvLecList.apply {
            hasFixedSize()
            adapter = stdLecListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.fabMarkAtd.setOnClickListener {
            val bundle = Bundle()
            val enrollment = arguments?.getString(FirebasePaths.STUDENT_ENROLLMENT)
            if(courseCode != null && enrollment != null) {
                bundle.putString(FirebasePaths.COURSE_CODE, courseCode)
                bundle.putString(FirebasePaths.SEM_SEC, sem_sec)
                bundle.putString(FirebasePaths.STUDENT_ENROLLMENT, enrollment)
                findNavController().navigate(R.id.action_stdCourseDetailsFragment_to_stdMarkAtdFragment, bundle)
            } else {
                Toast.makeText(context, "courseCode/enrollment is null", Toast.LENGTH_SHORT).show()
                Log.i(TAG, "setupViews: ERROR - courseCode/enrollment is null")
            }
        }
    }
}