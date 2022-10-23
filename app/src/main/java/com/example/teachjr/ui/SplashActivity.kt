package com.example.teachjr.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.teachjr.databinding.ActivitySplashBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.professor.ProfessorActivity
import com.example.teachjr.ui.student.StudentActivity
import com.example.teachjr.ui.viewmodels.SplashViewModel
import com.example.teachjr.utils.Response
import com.example.teachjr.utils.UserType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val TAG = SplashActivity::class.java.simpleName
    private lateinit var binding: ActivitySplashBinding
    private val splashViewModel by viewModels<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i(TAG, "SplashTesting: Splash Activity Created")

        splashViewModel.userType.observe(this) {
            when(it) {
                is Response.Loading -> {}
                is Response.Error -> {
                    Log.i(TAG, "SplashTesting: Error - ${it.errorMessage}")
                    Toast.makeText(this, it.errorMessage, Toast.LENGTH_LONG).show()
                }
                is Response.Success -> {
                    when(it.data) {
                        is UserType.Student -> {
                            val intent = Intent(this, StudentActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is UserType.Teacher -> {
                            val intent = Intent(this, ProfessorActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        null -> {
                            val intent = Intent(this, AuthActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
                }
            }
        }
    }
}