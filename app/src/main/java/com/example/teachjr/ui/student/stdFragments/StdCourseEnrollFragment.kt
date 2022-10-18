package com.example.teachjr.ui.student.stdFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdCourseEnrollBinding
import com.example.teachjr.ui.professor.profFragments.ProfCourseCreateFragment
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.ui.viewmodels.StudentViewModel
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
    }

    private fun enrollCourse() {
        val courseLink = binding.etCourseLink.text.toString()
        if(courseLink.isBlank()) {
            Toast.makeText(context, "Please enter course link", Toast.LENGTH_SHORT).show()
            return
        }

        studentViewModel.enrollCourse(courseLink)
    }

}