package com.example.teachjr.ui.professor.profFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentProfStdListBinding

class profStdListFragment : Fragment() {

    private lateinit var binding: FragmentProfStdListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfStdListBinding.inflate(layoutInflater)
        return binding.root
    }

}