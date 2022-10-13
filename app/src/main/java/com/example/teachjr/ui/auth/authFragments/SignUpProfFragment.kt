package com.example.teachjr.ui.auth.authFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentSignUpProfBinding
import com.example.teachjr.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpProfFragment : Fragment() {

    private lateinit var binding: FragmentSignUpProfBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpProfBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            createProfUser()
        }

        binding.layoutLogin.setOnClickListener {
            findNavController().navigate(R.id.action_signUpProfFragment_to_roleSelectFragment)
        }
    }

    private fun createProfUser() {
        val name = binding.etFullName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if(name.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Please enter all credentials", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Call viewmodel's profSignUp method
        Toast.makeText(context, "user Registered successfully", Toast.LENGTH_SHORT).show()
    }
}