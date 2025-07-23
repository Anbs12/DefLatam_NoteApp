package com.example.deflatam_noteapp.utils


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.deflatam_noteapp.model.Nota
import com.example.deflatam_noteapp.utils.receivers.NotificationReceiver

/**
 * Objeto de utilidad para programar y cancelar alarmas para los recordatorios.
 * Encapsula la interacción con el AlarmManager del sistema.
 */
object AlarmScheduler {

    /**
     * Programa una alarma para una nota específica.
     * La alarma activará el NotificationReceiver a la hora del recordatorio.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    fun schedule(context: Context, nota: Nota) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val recordatorio = nota.recordatorio ?: return // No hacer nada si no hay recordatorio

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_NOTA_ID, nota.id)
            putExtra(NotificationReceiver.EXTRA_NOTA_TITULO, nota.titulo)
            putExtra(NotificationReceiver.EXTRA_NOTA_CONTENIDO, nota.contenido)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nota.id.toInt(), // Usamos el ID de la nota como request code único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Se necesita permiso explícito para programar alarmas exactas en Android 12+
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                recordatorio.time,
                pendingIntent
            )
        }
    }

    /**
     * Cancela una alarma previamente programada para una nota.
     * Es importante llamar a esto cuando se elimina una nota o su recordatorio.
     */
    fun cancel(context: Context, nota: Nota) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            nota.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }
}
