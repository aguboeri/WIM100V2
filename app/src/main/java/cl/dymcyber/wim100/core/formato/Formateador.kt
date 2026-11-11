package cl.dymcyber.wim100.core.formato

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Presentacion de fechas, pesos y puntajes. Solo da formato, no decide nada. */
object Formateador {

    private val FECHA_CORTA = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))
    private val FECHA_CON_HORA = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL"))

    fun fecha(instante: Long): String = FECHA_CORTA.format(Date(instante))

    fun fechaYHora(instante: Long): String = FECHA_CON_HORA.format(Date(instante))

    fun puntaje(valor: Double): String = String.format(Locale("es", "CL"), "%.1f", valor)

    fun pesoArchivo(bytes: Long): String = when {
        bytes >= 1024 * 1024 -> String.format(Locale("es", "CL"), "%.1f MB", bytes / 1024.0 / 1024.0)
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }

    fun anios(cantidad: Int): String = if (cantidad == 1) "1 año" else "$cantidad años"
}
