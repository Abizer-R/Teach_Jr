package com.example.teachjr.ui.student

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.ActivityStudentBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StudentActivity : AppCompatActivity() {

    private val TAG = StudentActivity::class.java.simpleName
    private lateinit var binding: ActivityStudentBinding

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogout.setOnClickListener {
            val firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth.signOut()
            Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, AuthActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        /**
         * Implementing Up Navigation button
         */
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.stdFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        // Check if androidx.navigation.ui.NavigationUI.setupActionBarWithNavController is imported
        // By default title in actionbar is used from the fragment label in navigation graph
        // To use the app name, remove label else if you want to add customized label specify it there
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}