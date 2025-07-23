package com.example.deflatam_noteapp.utils.receivers


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.deflatam_noteapp.DetalleNotaActivity
import com.example.deflatam_noteapp.R
import com.example.deflatam_noteapp.data.NotasManager
import com.example.deflatam_noteapp.utils.AlarmScheduler
import com.example.deflatam_noteapp.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 * BroadcastReceiver que maneja tanto las alarmas de recordatorio
 * como el evento de reinicio del dispositivo para reprogramar alarmas.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_NOTA_ID = "extra_nota_id_receiver"
        const val EXTRA_NOTA_TITULO = "extra_nota_titulo_receiver"
        const val EXTRA_NOTA_CONTENIDO = "extra_nota_contenido_receiver"
    }

    /**
     * Se llama cuando el BroadcastReceiver recibe un Intent.
     * Distingue si es una alarma normal o un evento de reinicio.
     */
    override fun onReceive(context: Context, intent: Intent) {
        // Si la acción es de reinicio, reprogramamos todas las alarmas pendientes.
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            reprogramarAlarmas(context)
        } else {
            // Si es una alarma normal, mostramos la notificación.
            mostrarNotificacion(context, intent)
        }
    }

    /**
     * Muestra la notificación para un recordatorio específico.
     * Construye y dispara la notificación usando los datos del Intent.
     */
    private fun mostrarNotificacion(context: Context, intent: Intent) {
        val notaId = intent.getLongExtra(EXTRA_NOTA_ID, -1L)
        if (notaId == -1L) return

        val titulo = intent.getStringExtra(EXTRA_NOTA_TITULO) ?: "Recordatorio"
        val contenido = intent.getStringExtra(EXTRA_NOTA_CONTENIDO) ?: "Tienes una nota pendiente."

        val resultIntent = Intent(context, DetalleNotaActivity::class.java).apply {
            putExtra(DetalleNotaActivity.EXTRA_NOTA_ID, notaId)
        }
        val resultPendingIntent = PendingIntent.getActivity(
            context,
            notaId.toInt(),
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationUtils.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asegúrate de tener este ícono
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(resultPendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notaId.toInt(), builder.build())
        }
    }

    /**
     * Reprograma las alarmas para todas las notas con recordatorios pendientes.
     * Se ejecuta en un hilo de fondo para no bloquear el proceso de arranque.
     */
    private fun reprogramarAlarmas(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val notasPendientes = NotasManager.obtenerNotasConRecordatorioPendiente()
            for (nota in notasPendientes) {
                AlarmScheduler.schedule(context, nota)
            }
        }
    }
}