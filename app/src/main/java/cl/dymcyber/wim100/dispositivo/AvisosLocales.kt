package cl.dymcyber.wim100.dispositivo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.dymcyber.wim100.R

/** Recurso nativo: notificaciones locales. */
object AvisosLocales {

    private const val CANAL = "wim_valoraciones"
    private const val ID_BASE = 2100

    /** El canal debe existir antes de la primera notificación (Android 8 o superior). */
    fun prepararCanal(contexto: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL,
            "Valoraciones WIM 100",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Confirma cuando se registra una valoración de la matriz WIM."
        }
        contexto.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
    }

    /** true si el sistema nos deja mostrar notificaciones ahora mismo. */
    fun permitido(contexto: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            contexto,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /** Muestra el aviso. */
    @SuppressLint("MissingPermission")
    fun avisarValoracion(contexto: Context, nombrePostulante: String, puntaje: Double) {
        if (!permitido(contexto)) return

        val aviso = NotificationCompat.Builder(contexto, CANAL)
            .setSmallIcon(R.drawable.ic_aviso_wim)
            .setContentTitle("Valoración registrada")
            .setContentText("$nombrePostulante obtuvo $puntaje puntos")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        runCatching {
            NotificationManagerCompat.from(contexto).notify(ID_BASE + nombrePostulante.hashCode() % 100, aviso)
        }
    }
}
