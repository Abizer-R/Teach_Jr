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
import com.example.teachjr.databinding.FragmentStdHomeBinding
import com.example.teachjr.ui.adapters.StdCourseListAdapter
import com.example.teachjr.ui.viewmodels.studentViewModels.StdHomeViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StdHomeFragment : Fragment() {

    private val TAG = StdHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentStdHomeBinding
    private val stdHomeViewModel by viewModels<StdHomeViewModel>()

    private lateinit var sem_sec: String
    private lateinit var enrollment: String

    private val profCourseListAdapter = StdCourseListAdapter(
        onItemClicked = { rvCourseListItem ->

            val bundle = Bundle()
            bundle.putString(FirebasePaths.COURSE_CODE, rvCourseListItem.courseCode)
            bundle.putString(FirebasePaths.COURSE_NAME, rvCourseListItem.courseName)
            bundle.putString(FirebasePaths.COURSE_PROF_NAME, rvCourseListItem.profName)
            bundle.putString(FirebasePaths.SEM_SEC, sem_sec)        // FOR UPCOMING FRAGMENTS
            bundle.putString(FirebasePaths.STUDENT_ENROLLMENT, enrollment)  // FOR UPCOMING FRAGMENTS
            findNavController().navigate(R.id.action_stdHomeFragment_to_stdCourseDetailsFragment, bundle)
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

        stdHomeViewModel.currUserStd.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    Log.i(TAG, "StudentTesting_HomePage: CurrUser_Error - ${it.errorMessage}")
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    Log.i(TAG, "StudentTesting_HomePage: StudentUser = ${it.data}")

                    sem_sec = it.data?.sem_sec!!
                    enrollment = it.data.enrollment!!
                    // Fetch the courseList only if we have user's details
                    stdHomeViewModel.getCourseList()
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
                    profCourseListAdapter.updateList(it.data!!)
                }
            }
        }

    }

}