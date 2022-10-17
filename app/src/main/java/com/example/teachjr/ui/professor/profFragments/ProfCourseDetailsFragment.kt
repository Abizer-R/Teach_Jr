package com.example.teachjr.ui.professor.profFragments

import android.R.attr.label
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
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

    private val TAG = ProfCourseCreateFragment::class.java.simpleName
    private lateinit var binding: FragmentProfCourseDetailsBinding
    private val profViewModel by activityViewModels<ProfViewModel>()

    private var courseId: String? = null
    private var courseCode: String? = null
    private var courseName: String? = null

    private var lecDoc: LecturesDocument? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfCourseDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialSetup()
        profViewModel.getLectureDoc(courseId!!)
    }

    private fun initialSetup() {
        courseCode = arguments?.getString(FirebasePaths.COURSE_CODE)
        binding.tvCourseCode.text = courseCode

        courseName = arguments?.getString(FirebasePaths.COURSE_NAME)
        binding.tvCourseName.text = courseName

        courseId = arguments?.getString(FirebasePaths.COURSE_ID)
        binding.tvCourseLink.text = courseId

        binding.layoutCourseLink.setOnClickListener {
            val clipboardManager = context?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText("CourseId", courseId))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.btnAtdReport.setOnClickListener {
            findNavController().navigate(R.id.action_profCourseDetailsFragment_to_profAtdReportFragment)
        }

        profViewModel.lectureDoc.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    Toast.makeText(context, "Error: ${it.errorMessage}", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "initialSetup: ERROR: ${it.errorMessage}")
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.GONE
                    lecDoc = it.data
                    binding.tvLecCount.text = "Total Lecture: " + lecDoc?.lecCount.toString()
                }
            }
        }
    }

}