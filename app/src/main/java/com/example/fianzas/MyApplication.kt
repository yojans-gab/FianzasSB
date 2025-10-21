package com.example.fianzas

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.net.toUri

class MyApplication : Application() {
    companion object {
        const val FIANZAS_CHANNEL_ID = "fianzas_vencimiento"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Vencimiento de Fianzas"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val descriptionText = "Notificaciones para fianzas próximas a vencer"

            // Si usas un sonido personalizado, pon archivo en res/raw/notification_sound.mp3
            val soundUri = "android.resource://$packageName/${R.raw.notification_sound}".toUri()

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(FIANZAS_CHANNEL_ID, channelName, importance).apply {
                description = descriptionText
                setSound(soundUri, audioAttributes) // sonido personalizado (o default si cambias uri)
                enableVibration(true)
                // Puedes ajustar lockscreen visibility, lights, etc. aquí si quieres.
            }

            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)

            // DEBUG: verifica que el canal fue creado y qué sound tiene
            val ch = nm.getNotificationChannel(FIANZAS_CHANNEL_ID)
            Log.d("MyApplication", "Canal creado: $ch")
        }
    }
}
