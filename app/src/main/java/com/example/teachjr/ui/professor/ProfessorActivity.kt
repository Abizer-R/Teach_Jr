package com.example.teachjr.ui.professor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.teachjr.R
import com.example.teachjr.databinding.ActivityProfessorBinding
import com.example.teachjr.databinding.ActivityStudentBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfessorActivity : AppCompatActivity() {

    private val TAG = ProfessorActivity::class.java.simpleName
    private lateinit var binding: ActivityProfessorBinding

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.i(TAG, "ProfessorTesting: Professor Activity Created")

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
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.profFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration
            .Builder(
                R.id.profHomeFragment,
                R.id.profMarkAtdFragment
            )
            .build()
        // Check if androidx.navigation.ui.NavigationUI.setupActionBarWithNavController is imported
        // By default title in actionbar is used from the fragment label in navigation graph
        // To use the app name, remove label else if you want to add customized label specify it there
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}