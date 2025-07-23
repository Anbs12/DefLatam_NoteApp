package com.example.deflatam_noteapp.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Objeto de utilidad para gestionar la creación de canales de notificación.
 * Esencial para la compatibilidad con Android 8.0 (Oreo) y superior.
 */
object NotificationUtils {

    const val CHANNEL_ID = "notas_reminders_channel"
    private const val CHANNEL_NAME = "Recordatorios de Notas"
    private const val CHANNEL_DESCRIPTION = "Canal para mostrar recordatorios de notas"

    /**
     * Crea el canal de notificación si la app se ejecuta en Android 8.0+.
     * Debe ser llamado una vez al iniciar la aplicación.
     */
    fun createNotificationChannel(context: Context) {
        // La creación del canal solo es necesaria en API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }

            // Registra el canal con el sistema
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
