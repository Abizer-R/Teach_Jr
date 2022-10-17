package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfCourseCreateBinding
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfCourseCreateFragment : Fragment() {

    private val TAG = ProfCourseCreateFragment::class.java.simpleName
    private lateinit var binding: FragmentProfCourseCreateBinding
    private val profViewModel by activityViewModels<ProfViewModel>()

    private var courseName = ""
    private var courseCode = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfCourseCreateBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreate.setOnClickListener {
            createCourse()
        }

        profViewModel.newCourseID.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> updateLoadingViews(true)

                is Response.Error -> {
                    updateLoadingViews(false, true)
                    binding.tvNewCourseLink.text = it.errorMessage
                }
                is Response.Success -> {
                    updateLoadingViews(false)
                    val bundle = Bundle()
                    bundle.putString(FirebasePaths.COURSE_ID, it.data)
                    bundle.putString(FirebasePaths.COURSE_CODE, courseCode)
                    bundle.putString(FirebasePaths.COURSE_NAME, courseName)
                    findNavController().navigate(R.id.action_profCourseCreateFragment_to_profCourseDetailsFragment, bundle)
                    binding.tvNewCourseLink.text = it.data
                }
            }
        }
    }

    private fun createCourse() {
        courseCode = binding.etCourseCode.text.toString()
        courseName = binding.etCourseName.text.toString()
        if(courseCode.isBlank() || courseName.isBlank()) {
            Toast.makeText(context, "Please enter both details", Toast.LENGTH_SHORT).show()
            return
        }

        profViewModel.createCourse(courseCode, courseName)
    }

    private fun updateLoadingViews(isLoading: Boolean, btnAgain: Boolean = false) {
        if(isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnCreate.isClickable = false
            binding.btnCreate.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            if(btnAgain) {
                binding.btnCreate.isClickable = true
                binding.btnCreate.isEnabled = true
            }
        }
    }



}