package com.example.teachjr.ui.auth.authFragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.FragmentLoginBinding
import com.example.teachjr.ui.professor.ProfessorActivity
import com.example.teachjr.ui.student.StudentActivity
import com.example.teachjr.ui.viewmodels.AuthViewModel
import com.example.teachjr.utils.Response
import com.example.teachjr.utils.UserType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val TAG = LoginFragment::class.java.simpleName
    private lateinit var binding: FragmentLoginBinding
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

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if(email.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Please enter both credentials", Toast.LENGTH_SHORT).show()
            } else {
                authViewModel.login(email, password)
            }
        }

        authViewModel.loginStatus.observe(viewLifecycleOwner) {
            when(it) {
                is Response.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, it.errorMessage, Toast.LENGTH_SHORT).show()
                }
                is Response.Success -> {
                    val type = it.data
                    when(type) {
                        is UserType.Student -> {
                            val intent = Intent(activity, StudentActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                        is UserType.Teacher -> {
                            val intent = Intent(activity, ProfessorActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                        null -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, "User is null", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.layoutSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_roleSelectFragment)
        }
    }

}