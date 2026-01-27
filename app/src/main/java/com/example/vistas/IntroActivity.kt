package com.example.vistas

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val container = findViewById<View>(R.id.mainContainer)

        container.setOnClickListener {
            irAlLogin()
        }
    }

    @Suppress("DEPRECATION") // Añade esto para ignorar el aviso
    private fun irAlLogin() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}