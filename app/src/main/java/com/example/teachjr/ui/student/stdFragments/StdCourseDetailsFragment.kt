package com.example.teachjr.ui.student.stdFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdCourseDetailsBinding
import com.example.teachjr.ui.adapters.StdLecListAdapter
import com.example.teachjr.ui.professor.profFragments.ProfCourseDetailsFragment
import com.example.teachjr.ui.viewmodels.StudentViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response

class StdCourseDetailsFragment : Fragment() {

    private val TAG = StdCourseDetailsFragment::class.java.simpleName
    private lateinit var binding: FragmentStdCourseDetailsBinding
    private val stdViewModel by activityViewModels<StudentViewModel>()

    private var courseCode: String? = null
    private var courseName: String? = null
    private var profName: String? = null

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
        initialSetup()
        setupViews()
        setupScrollView()
    }

    private fun initialSetup() {
        courseCode = arguments?.getString(FirebasePaths.COURSE_CODE)
        courseName = arguments?.getString(FirebasePaths.COURSE_NAME)
        profName = arguments?.getString(FirebasePaths.COURSE_PROF_NAME)

        if(courseCode == null || courseName == null || profName == null) {
            Log.i(TAG, "StudentTesting_CoursePage: null bundle arguments")
            Toast.makeText(context, "Couldn't fetch all details, Some might be null", Toast.LENGTH_SHORT).show()
        }
        binding.tvCourseCode.text = courseCode
        binding.tvCourseName.text = courseName
        binding.tvProfName.text = profName

        stdViewModel.getLecAttended(courseCode)
        stdViewModel.atdDetails.observe(viewLifecycleOwner) {
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

                    stdLecListAdapter.updateList(it.data.lecList)
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
            bundle.putString(FirebasePaths.COURSE_CODE, courseCode)
            findNavController().navigate(R.id.action_stdCourseDetailsFragment_to_stdMarkAtdFragment, bundle)
        }
    }

    private fun setupScrollView() {
        binding.svStdCourseDetails.setOnScrollChangeListener(object: NestedScrollView.OnScrollChangeListener {
            override fun onScrollChange(
                v: NestedScrollView,
                scrollX: Int,
                scrollY: Int,
                oldScrollX: Int,
                oldScrollY: Int
            ) {
                if(scrollY > oldScrollY) {
                    binding.fabMarkAtd.hide()
                } else {
                    binding.fabMarkAtd.show()
                }

            }

        })
    }
}