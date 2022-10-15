package com.example.teachjr.ui.professor

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.teachjr.R
import com.example.teachjr.databinding.ActivityProfessorBinding
import com.example.teachjr.databinding.ActivityStudentBinding
import com.example.teachjr.ui.auth.AuthActivity
import com.example.teachjr.ui.student.StudentActivity
import com.google.firebase.auth.FirebaseAuth

class ProfessorActivity : AppCompatActivity() {

    private val TAG = ProfessorActivity::class.java.simpleName
    private lateinit var binding: ActivityProfessorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessorBinding.inflate(layoutInflater)
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
    }
}