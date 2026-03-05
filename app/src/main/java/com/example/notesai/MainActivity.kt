package com.example.notesai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide ActionBar
        supportActionBar?.hide()

        //Splash Screen
        Handler(Looper.getMainLooper()).postDelayed({

            val intent = Intent(this, HomeActivity2::class.java)
            startActivity(intent)
            finish()

        }, 3000)
    }
}