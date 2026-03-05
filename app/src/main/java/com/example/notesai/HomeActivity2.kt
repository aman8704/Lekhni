package com.example.notesai

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class HomeActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home2)

        val notesButton =findViewById<LinearLayout>(R.id.notesBtn)
        notesButton.setOnClickListener {
            val intent = Intent(this, NotesActivity3::class.java)
            startActivity(intent)
        }
    }
}
