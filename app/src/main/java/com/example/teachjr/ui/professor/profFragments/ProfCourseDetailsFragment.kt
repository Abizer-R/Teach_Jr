package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.data.model.LecturesDocument
import com.example.teachjr.databinding.FragmentProfCourseDetailsBinding
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.utils.FirebasePaths
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfCourseDetailsFragment : Fragment() {

    private val TAG = ProfCourseDetailsFragment::class.java.simpleName
    private lateinit var binding: FragmentProfCourseDetailsBinding
    private val profViewModel by activityViewModels<ProfViewModel>()

    private var courseCode: String? = null
    private var courseName: String? = null
    private var semSec: String? = null

//    private var lecDoc: LecturesDocument? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfCourseDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialSetup()
    }

    private fun initialSetup() {
        courseCode = arguments?.getString(FirebasePaths.COURSE_CODE)
        binding.tvCourseCode.text = courseCode

        courseName = arguments?.getString(FirebasePaths.NAME)
        binding.tvCourseName.text = courseName

        semSec = arguments?.getString(FirebasePaths.SEM_SEC)
        profViewModel.getLectureCount(semSec!!, courseCode!!)

        binding.btnAtdReport.setOnClickListener {
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profAtdReportFragment)
        }

        profViewModel.lecCount.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    Toast.makeText(context, "Error: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvLecCount.text = "Total Lectures: ${it.data.toString()}"
                }
            }
        }
    }

}