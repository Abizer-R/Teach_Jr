package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfHomeBinding
import com.example.teachjr.ui.adapters.CourseListAdapter
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfHomeFragment : Fragment() {

    private val TAG = ProfHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentProfHomeBinding
    private val profViewModel by activityViewModels<ProfViewModel>()

    private val courseListAdapter = CourseListAdapter(
        onItemClicked = { rvCourseItem ->

            // TODO: PASS WHOLE COURSE LIST OBJECT TO OTHER FRAGMENT....
            // We need courseCode and courseName as well

            val bundle = Bundle()
            bundle.putString(FirebasePaths.COURSE_ID, rvCourseItem.courseId)
            bundle.putString(FirebasePaths.COURSE_CODE, rvCourseItem.courseCode)
            bundle.putString(FirebasePaths.COURSE_NAME, rvCourseItem.courseName)
            findNavController().navigate(R.id.action_profHomeFragment_to_profCourseDetailsFragment, bundle)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvCourseList.apply {
            adapter = courseListAdapter
            layoutManager = LinearLayoutManager(context)
        }

        binding.fabCreateCourse.setOnClickListener {
            findNavController().navigate(R.id.action_profHomeFragment_to_profCourseCreateFragment)
        }

        profViewModel.getCourseDocs()
        profViewModel.courseDocs.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "Get Courses Failed: ${it.errorMessage}")
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "New data loaded", Toast.LENGTH_SHORT).show()
                    courseListAdapter.updateList(profViewModel.getRvCourseItemList(it.data!!))
                }
            }
        }
    }
}