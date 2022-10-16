package com.example.teachjr.ui.professor.profFragments

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
import com.example.teachjr.databinding.FragmentProfHomeBinding
import com.example.teachjr.ui.viewmodels.ProfViewModel
import com.example.teachjr.utils.Response
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfHomeFragment : Fragment() {

    private val TAG = ProfHomeFragment::class.java.simpleName
    private lateinit var binding: FragmentProfHomeBinding
    private val profViewModel by activityViewModels<ProfViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabCreateCourse.setOnClickListener {
            findNavController().navigate(R.id.action_profHomeFragment_to_profCourseCreateFragment)
        }

//        profViewModel.currUser.observe(viewLifecycleOwner) {
//            when(it) {
//                is Response.Error -> {
//                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
//                    Log.i(TAG, "GET USER DETAILS: ${it.errorMessage}")
//                }
//                is Response.Loading -> {
//                    Toast.makeText(context, "Loading User Data", Toast.LENGTH_SHORT).show()
//                    Log.i(TAG, "GET USER DETAILS: LOADING")
//                }
//                is Response.Success -> {
//                    Log.i(TAG, "GET USER DETAILS: ${it.data}")
//                }
//            }
//        }
    }
}