package com.example.teachjr.ui.student.stdFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdHomeBinding
import com.example.teachjr.ui.professor.profFragments.ProfHomeFragment
import com.example.teachjr.ui.viewmodels.StudentViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StdHomeFragment : Fragment() {

    private val TAG = StdHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentStdHomeBinding
    private val studentViewModel by activityViewModels<StudentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStdHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Get course Lists

        binding.fabEnroll.setOnClickListener {
            findNavController().navigate(R.id.action_stdHomeFragment_to_stdCourseEnrollFragment)
        }
    }

}