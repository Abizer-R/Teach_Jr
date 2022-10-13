package com.example.teachjr.ui.auth.authFragments

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentLoginBinding
import com.example.teachjr.ui.auth.AuthViewModel
import com.example.teachjr.ui.auth.UserType
import com.example.teachjr.utils.FirebaseConstants
import com.example.teachjr.utils.FirebaseConstants.userType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val TAG = LoginFragment::class.java.simpleName
    private lateinit var binding: FragmentLoginBinding
//    private lateinit var userTpye: String
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialSetup()
    }

    private fun initialSetup() {
        authViewModel.userType.observe(viewLifecycleOwner) {
            it?.apply {
                when(this) {
                    is UserType.Student -> {
                        setupStudentLogin()
                    }
                    is UserType.Teacher -> {
                        setupProfessorLogin()
                    }
                }
            }
        }

        binding.layoutSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_roleSelectFragment)
        }
    }

    private fun setupStudentLogin() {
        binding.tvLogin.text = "Hey mate.\nEnter your credentials"

        binding.btnLogin.setOnClickListener {
            if(checkFields()) {
                Toast.makeText(context, "Please enter both credentials", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Implement ViewModel's login and move to StudentActivity
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
                // TODO: Implement ViewModel's login and move to ProfessorActivity
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