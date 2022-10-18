package com.example.teachjr.ui.student.stdFragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdCourseEnrollBinding
import com.example.teachjr.ui.professor.profFragments.ProfCourseCreateFragment
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.ui.viewmodels.StudentViewModel
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StdCourseEnrollFragment : Fragment() {

    private val TAG = ProfCourseCreateFragment::class.java.simpleName
    private lateinit var binding: FragmentStdCourseEnrollBinding
    private val studentViewModel by activityViewModels<StudentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStdCourseEnrollBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnEnroll.setOnClickListener {
            enrollCourse()
        }

        studentViewModel.enrollCourseStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> updateLoadingViews(true)
                is Response.Error -> {
                    updateLoadingViews(false, true)
                    binding.tvRequestStatus.text = it.errorMessage
                }
                is Response.Success -> {
                    val newRequestCreated = it.data
                    when(newRequestCreated) {
                        true -> {
                            // TODO: Design the course Detail fragment
                            // TODO: send data to view through bundle
                            // TODO: You already have courseId
                            // TODO: USE ENROLL_REPO TO FETCH profName, courseCode, courseName
                            findNavController()
                        }
                        false -> {
                            updateLoadingViews(false)
                            binding.tvRequestStatus.text = "ALREADY REQUESTED. APPROVAL PENDING."
                        }
                        null -> {
                            updateLoadingViews(false, true)
                            binding.tvRequestStatus.text = "Success response is null"
                        }
                    }
                }
            }
        }
    }

    private fun enrollCourse() {
        val courseLink = binding.etCourseLink.text.toString()
        if(courseLink.isBlank()) {
            Toast.makeText(context, "Please enter course link", Toast.LENGTH_SHORT).show()
            return
        }

        studentViewModel.enrollCourse(courseLink)
    }

    private fun updateLoadingViews(isLoading: Boolean, btnAgain: Boolean = false) {
        if(isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnEnroll.isClickable = false
            binding.btnEnroll.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            if(btnAgain) {
                binding.btnEnroll.isClickable = true
                binding.btnEnroll.isEnabled = true
            }
        }
    }

}