package com.example.fianzas

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Splash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        VerBienvenida()
    }

    fun VerBienvenida() {
        object : CountDownTimer(2000, 1000) {
            override fun onFinish() {
                //Regirigir a la actividad MainActivity
                val intent = Intent(this@Splash, MainActivity::class.java)
                startActivity(intent)
                finishAffinity()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()
    }
}