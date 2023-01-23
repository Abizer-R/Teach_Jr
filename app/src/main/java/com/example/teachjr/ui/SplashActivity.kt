package com.example.teachjr.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import com.example.teachjr.databinding.ActivitySplashBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.professor.ProfessorActivity
import com.example.teachjr.ui.student.StudentActivity
import com.example.teachjr.ui.viewmodels.SplashViewModel
import com.example.teachjr.utils.Permissions
import com.example.teachjr.utils.sealedClasses.Response
import com.example.teachjr.utils.sealedClasses.UserType
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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        Permissions.requestPermission(this@SplashActivity)
        Permissions.requestStoragePermissions(this@SplashActivity)

        splashViewModel.userType.observe(this) {
            when(it) {
                is Response.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Response.Error -> {
                    Log.i(TAG, "SplashTesting: Error - ${it.errorMessage}")
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(this, it.errorMessage, Toast.LENGTH_LONG).show()
                }
                is Response.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
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