package com.example.teachjr.ui.auth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.teachjr.R
import com.example.teachjr.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
    }
}