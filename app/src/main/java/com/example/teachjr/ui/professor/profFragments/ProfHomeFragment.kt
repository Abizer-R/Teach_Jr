package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfHomeBinding
import com.example.teachjr.ui.adapters.ProfCourseListAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfHomeViewModel
import com.example.teachjr.ui.viewmodels.professorViewModels.SharedProfViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfHomeFragment : Fragment() {

    private val TAG = ProfHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentProfHomeBinding

    private val homeViewModel by viewModels<ProfHomeViewModel>()
    private val sharedProfViewModel by activityViewModels<SharedProfViewModel>()

    private val profCourseListAdapter = ProfCourseListAdapter(
        onItemClicked = { rvCourseItem ->

            // Update the course values
            sharedProfViewModel.updateCourseDetails(
                rvCourseItem.courseCode,
                rvCourseItem.courseName,
                rvCourseItem.sem_sec
            )
            findNavController().navigate(R.id.action_profHomeFragment_to_profCourseDetailsFragment)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfHomeBinding.inflate(layoutInflater)
        Log.i(TAG, "ProfessorTesting_HomePage: Professor HomePage Created")
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
        sharedProfViewModel.clearValues()

        /**
         * Fetch the courseList only if we have just launched our app
         */
        if(sharedProfViewModel.courseList != null) {
            populateCourseRv()
        } else {
            homeViewModel.getCourseList()
            setObservers()
        }
    }

    private fun setObservers() {
        homeViewModel.courseList.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    Log.i(TAG, "ProfessorTesting_HomePage: CourseList_Error - ${it.errorMessage}")
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "ProfessorTesting_HomePage: CourseList - ${it.data}")
                    binding.progressBar.visibility = View.GONE

                    if(it.data != null) {
                        sharedProfViewModel.setCourseList(it.data)
                        populateCourseRv()
                    } else {
                        Toast.makeText(context, "Course List is null. Refresh", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun populateCourseRv() {
        profCourseListAdapter.updateList(sharedProfViewModel.courseList!!)
    }
}