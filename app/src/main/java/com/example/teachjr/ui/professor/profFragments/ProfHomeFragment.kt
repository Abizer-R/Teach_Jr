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
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfHomeFragment : Fragment() {

    private val TAG = ProfHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentProfHomeBinding
    private val profHomeViewModel by viewModels<ProfHomeViewModel>()

    private val profCourseListAdapter = ProfCourseListAdapter(
        onItemClicked = { rvCourseItem ->

            // send courseCode, courseName and sem_sec
            val bundle = Bundle()
            bundle.putString(FirebasePaths.COURSE_CODE, rvCourseItem.courseCode)
            bundle.putString(FirebasePaths.COURSE_NAME, rvCourseItem.courseName)
            bundle.putString(FirebasePaths.SEM_SEC, rvCourseItem.sem_sec)
            findNavController().navigate(R.id.action_profHomeFragment_to_profCourseDetailsFragment, bundle)
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

        profHomeViewModel.getCourseList()
        profHomeViewModel.courseList.observe(viewLifecycleOwner) {
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
                    profCourseListAdapter.updateList(it.data!!)
                }
            }
        }
    }
}