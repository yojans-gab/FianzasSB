package com.example.fianzas

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fianzas.Administrador.MenuAdministrador
import com.example.fianzas.Administrador.RegistrarUsuario
import com.example.fianzas.Gestor.MenuGestor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Splash : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        firebaseAuth = FirebaseAuth.getInstance()
        VerBienvenida()
    }

    fun VerBienvenida() {
        object : CountDownTimer(2000, 1000) {
            override fun onFinish() {
                ComprobarSesion()
            }

            override fun onTick(millisUntilFinished: Long) {

            }

        }.start()
    }

    fun prueva(){
        startActivity(Intent(this, RegistrarUsuario::class.java))
        finishAffinity()
    }
    fun ComprobarSesion(){
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }
        else{
            val reference = FirebaseDatabase.getInstance().getReference("Usuario")
            reference.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val rol = "${snapshot.child("rol").value}"
                        if (rol == "Administrador"){
                            startActivity(Intent(this@Splash, MenuAdministrador::class.java))
                            finishAffinity()
                        }
                        else if (rol == "Gestor"){
                            startActivity(Intent(this@Splash, MenuGestor::class.java))
                            finishAffinity()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }
    }
}