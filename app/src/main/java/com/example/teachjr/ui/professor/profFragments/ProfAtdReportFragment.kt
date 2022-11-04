package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfAtdReportBinding

class ProfAtdReportFragment : Fragment() {

    private lateinit var binding: FragmentProfAtdReportBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfAtdReportBinding.inflate(layoutInflater)
        return binding.root
    }

}