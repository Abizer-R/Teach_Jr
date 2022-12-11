package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentAtdReportLecturesBinding
import com.example.teachjr.databinding.FragmentAtdReportStudentsBinding
import com.example.teachjr.ui.adapters.AtdReportAdapter
import com.example.teachjr.ui.viewmodels.professorViewModels.ProfAtdReportViewModel
import com.example.teachjr.utils.AdapterUtils
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AtdReportLecturesFragment : Fragment() {

    private val TAG = AtdReportLecturesFragment::class.java.simpleName
    private lateinit var binding: FragmentAtdReportLecturesBinding
    private val atdReportViewModel by activityViewModels<ProfAtdReportViewModel>()

    private val atdReportAdapter = AtdReportAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAtdReportLecturesBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvLecAtdReport.apply {
            hasFixedSize()
            adapter = atdReportAdapter
            layoutManager = LinearLayoutManager(context)
        }

        atdReportViewModel.atdDetails.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Response.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "${it.errorMessage}", Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.GONE

                    val stdCount = it.data!!.studentList.size
                    val lecList = it.data.lectureList
                    val lecPercentageList = AdapterUtils.getLecturePercentage(stdCount, lecList)

                    atdReportAdapter.updateList(lecPercentageList)
                }
            }
        }
    }

}