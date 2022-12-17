package com.example.teachjr.ui.student.stdFragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdHomeBinding
import com.example.teachjr.ui.adapters.StdCourseListAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.ui.viewmodels.studentViewModels.SharedStdViewModel
import com.example.teachjr.ui.viewmodels.studentViewModels.StdHomeViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StdHomeFragment : Fragment() {

    private val TAG = StdHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentStdHomeBinding

    private val stdHomeViewModel by viewModels<StdHomeViewModel>()
    private val sharedStdViewModel by activityViewModels<SharedStdViewModel>()

    private val profCourseListAdapter = StdCourseListAdapter(
        onItemClicked = { rvCourseListItem ->

            if(sharedStdViewModel.userDetails != null) {

            }
            sharedStdViewModel.updateCourseDetails(
                rvCourseListItem.courseCode,
                rvCourseListItem.courseName,
                rvCourseListItem.profName
            )
            findNavController().navigate(R.id.action_stdHomeFragment_to_stdCourseDetailsFragment)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStdHomeBinding.inflate(layoutInflater)
        Log.i(TAG, "StudentTesting_HomePage: Student HomePage Created")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCourseList.apply {
            hasFixedSize()
            adapter = profCourseListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        /**
         * Making sure if when we navigate up from a courseDetail page,
         * the previous values are cleared
         */
        sharedStdViewModel.clearCourseValues()

        /**
         * Fetch the user details and courseList only if we have just launched our app
         */
        if(sharedStdViewModel.userDetails != null && sharedStdViewModel.courseList != null) {
            populateCourseRv()
        } else {
            stdHomeViewModel.getUser()
            setObservers()
        }

    }

    private fun setObservers() {
        stdHomeViewModel.currUserStd.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
                    // TODO: Hide the progress bar only when the course List is populated.... CHANGE THE CODE
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    Log.i(TAG, "StudentTesting_HomePage: CurrUser_Error - ${it.errorMessage}")
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "StudentTesting_HomePage: StudentUser = ${it.data}")

                    if(it.data != null) {
                        sharedStdViewModel.setUserDetails(it.data)

                        // Fetch the courseList only if we have user's details
                        stdHomeViewModel.getCourseList(it.data.institute!!, it.data.branch!!, it.data.sem_sec!!)
                    } else {

                        // TODO: Add a refresh button
                        Toast.makeText(context, "User Details are null. Refresh", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        stdHomeViewModel.courseList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    Log.i(TAG, "StudentTesting_HomePage: CourseList_Error - ${it.errorMessage}")
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "StudentTesting_HomePage: CourseList = ${it.data}")
                    binding.progressBar.visibility = View.GONE
                    if(it.data != null) {
                        sharedStdViewModel.setCourseList(it.data)
                        populateCourseRv()
                    } else {
                        Toast.makeText(context, "Course List is null. Refresh", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun populateCourseRv() {
        profCourseListAdapter.updateList(sharedStdViewModel.courseList!!)
    }

}