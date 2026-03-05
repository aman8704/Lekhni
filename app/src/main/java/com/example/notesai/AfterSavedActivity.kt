package com.example.notesai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AfterSavedActivity : AppCompatActivity() {

    private lateinit var pdfUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_after_saved)

        // ✅ Get PDF URI safely
        val uriString = intent.getStringExtra("pdf_uri")

        if (uriString == null) {
            Toast.makeText(this, "PDF not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        pdfUri = Uri.parse(uriString)

        val btnOpen = findViewById<Button>(R.id.btnOpenPdf)
        val btnShare = findViewById<Button>(R.id.btnSharePdf)

        // 📂 OPEN PDF
        btnOpen.setOnClickListener {
            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(pdfUri, "application/pdf")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(openIntent)
        }

        // 📤 SHARE PDF
        btnShare.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, pdfUri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            startActivity(Intent.createChooser(shareIntent, "Share PDF"))
        }
    }
}