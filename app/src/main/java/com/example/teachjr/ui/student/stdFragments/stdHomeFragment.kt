package com.example.teachjr.ui.student.stdFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentStdHomeBinding

class stdHomeFragment : Fragment() {

    private lateinit var binding: FragmentStdHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStdHomeBinding.inflate(layoutInflater)
        return binding.root
    }

}