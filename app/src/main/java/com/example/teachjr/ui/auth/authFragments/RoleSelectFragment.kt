package com.example.teachjr.ui.auth.authFragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentRoleSelectBinding
import com.example.teachjr.ui.auth.AuthViewModel
import com.example.teachjr.ui.auth.UserType
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebaseConstants.userType
import dagger.hilt.android.AndroidEntryPoint
import hilt_aggregated_deps._dagger_hilt_android_internal_modules_ApplicationContextModule
import kotlinx.coroutines.NonDisposableHandle.parent

@AndroidEntryPoint
class RoleSelectFragment : Fragment() {

    private lateinit var binding: FragmentRoleSelectBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoleSelectBinding.inflate(inflater, container, false)

        // TODO: IMPLEMENT TWO WAY BINDING
        binding.layoutStd.setOnClickListener {
            authViewModel.setUserType(FirebaseConstants.typeStudent)
        }

        // TODO: IMPLEMENT TWO WAY BINDING
        binding.layoutProf.setOnClickListener {
            authViewModel.setUserType(FirebaseConstants.typeProfessor)
        }

        authViewModel.userType.observe(viewLifecycleOwner) {
            when(it) {
                is UserType.Student -> {
                    selectStudent()
                    unSelectProfessor()
                }
                is UserType.Teacher -> {
                    selectProfessor()
                    unSelectStudent()
                }
            }
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_roleSelectFragment_to_loginFragment)
        }

        binding.btnSignUp.setOnClickListener {
            gotoSignUpScreen()
        }
        return binding.root
    }


    private fun gotoSignUpScreen() {
        if(authViewModel.userType.value == UserType.Student()) {
            findNavController().navigate(R.id.action_roleSelectFragment_to_signUpStdFragment)
        } else {
            findNavController().navigate(R.id.action_roleSelectFragment_to_signUpProfFragment)
        }
    }

    private fun selectStudent() {
        binding.imgStd.alpha = 1F
        binding.tvStdLabel.apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun selectProfessor() {
        binding.imgProf.alpha = 1F
        binding.tvProfLabel.apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
        }
    }

    private fun unSelectStudent() {
        binding.imgStd.alpha = 0.6F
        binding.tvStdLabel.apply {
            typeface = Typeface.DEFAULT
            setTextColor(Color.parseColor("#51A2A8AD"))
        }
    }

    private fun unSelectProfessor() {
        binding.imgProf.alpha = 0.6F
        binding.tvProfLabel.apply {
            typeface = Typeface.DEFAULT
            setTextColor(Color.parseColor("#51A2A8AD"))
        }
    }
}