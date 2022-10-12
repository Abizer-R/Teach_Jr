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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentRoleSelectBinding
import com.example.teachjr.utils.FirebaseConstants
import hilt_aggregated_deps._dagger_hilt_android_internal_modules_ApplicationContextModule
import kotlinx.coroutines.NonDisposableHandle.parent

class RoleSelectFragment : Fragment() {

    private lateinit var binding: FragmentRoleSelectBinding

    private var isStdSelected = false
    private var isProfSelected = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoleSelectBinding.inflate(inflater, container, false)

        binding.layoutStd.setOnClickListener {
            selectStudent()
            unSelectProfessor()
        }

        binding.layoutProf.setOnClickListener {
            selectProfessor()
            unSelectStudent()
        }

        binding.btnLogin.setOnClickListener {
            gotoLoginScreen()
        }

        binding.btnSignUp.setOnClickListener {
            gotoSignUpScreen()
        }

        return binding.root
    }

    private fun gotoLoginScreen() {
        val bundle: Bundle
        if(isStdSelected) {
            bundle = bundleOf(
                FirebaseConstants.userType to FirebaseConstants.typeStudent)
        } else if(isProfSelected) {
            bundle = bundleOf(
                FirebaseConstants.userType to FirebaseConstants.typeProfessor)
        } else {
            Toast.makeText(context, "Please select your role", Toast.LENGTH_SHORT).show()
            return
        }
        findNavController().navigate(R.id.action_roleSelectFragment_to_loginFragment, bundle)
    }

    private fun gotoSignUpScreen() {
        if(isStdSelected) {
            findNavController().navigate(R.id.action_roleSelectFragment_to_signUpStdFragment)
        } else if(isProfSelected) {
            findNavController().navigate(R.id.action_roleSelectFragment_to_signUpProfFragment)
        } else {
            Toast.makeText(context, "Please select your role", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectStudent() {
        binding.imgStd.alpha = 1F
        binding.tvStdLabel.apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
        }
        isStdSelected = true
    }

    private fun selectProfessor() {
        binding.imgProf.alpha = 1F
        binding.tvProfLabel.apply {
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.parseColor("#000000"))
        }
        isProfSelected = true
    }

    private fun unSelectStudent() {
        binding.imgStd.alpha = 0.6F
        binding.tvStdLabel.apply {
            typeface = Typeface.DEFAULT
            setTextColor(Color.parseColor("#51A2A8AD"))
        }
        isStdSelected = false
    }

    private fun unSelectProfessor() {
        binding.imgProf.alpha = 0.6F
        binding.tvProfLabel.apply {
            typeface = Typeface.DEFAULT
            setTextColor(Color.parseColor("#51A2A8AD"))
        }
        isProfSelected = false
    }
}