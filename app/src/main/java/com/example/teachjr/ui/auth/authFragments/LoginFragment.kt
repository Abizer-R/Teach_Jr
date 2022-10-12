package com.example.teachjr.ui.auth.authFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.teachjr.databinding.FragmentLoginBinding
import com.example.teachjr.utils.FirebaseConstants

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        val userType: String = arguments?.getString(FirebaseConstants.userType)!!
        if(userType.equals(FirebaseConstants.typeStudent)) {
            setupStudentLogin()
        } else {
            setupProfessorLogin()
        }

        return binding.root
    }

    private fun setupStudentLogin() {
        binding.tvLogin.text = "Hey mate.\nEnter your credentials"

        binding.btnLogin.setOnClickListener {
            if(checkFields()) {
                Toast.makeText(context, "Please enter both credentials", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Implement ViewModel's login
                Toast.makeText(context, "Student Login Successful", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupProfessorLogin() {
        binding.tvLogin.text = "Hello Professor.\nEnter your credentials"

        binding.btnLogin.setOnClickListener {
            if(checkFields()) {
                Toast.makeText(context, "Please enter both credentials", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Implement ViewModel's login
                Toast.makeText(context, "Professor Login Successful", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkFields(): Boolean {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        return email.isBlank() || password.isBlank()
    }

}