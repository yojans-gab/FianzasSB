package com.example.fianzas

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // La notificación llega con datos, la mostramos.
        remoteMessage.notification?.let { notification ->
            val title = notification.title ?: "Fianza por Vencer"
            val body = notification.body ?: "Revisa los detalles."
            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        // 1. Usamos el ID del canal definido en MyApplication.
        // El canal ya está creado y configurado con sonido.
        val channelId = MyApplication.FIANZAS_CHANNEL_ID

        // 2. Creamos un Intent para abrir la Activity deseada.
        // REEMPLAZA 'ListaFianzasActivity::class.java' con la Activity a la que quieres ir.
        val intent = Intent(this, ListaFianzasActivity::class.java).apply {
            // flags para manejar la pila de navegación correctamente
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 3. Creamos el PendingIntent que envolverá nuestro Intent.
        // El PendingIntent le da permiso al sistema de notificaciones para ejecutar el Intent en nombre de tu app.
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, // requestCode
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // Flags de seguridad e inmutabilidad
        )

        // 4. Construimos la notificación como antes, pero ahora añadimos el PendingIntent.
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_calendario) // Asegúrate de que este ícono exista
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true) // La notificación se cierra al tocarla
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) // ¡ESTA ES LA LÍNEA CLAVE!

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Mostramos la notificación. Usamos un ID único para no sobreescribir notificaciones.
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}