package cl.dymcyber.wim100.core.formato

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Presentación de fechas, pesos y puntajes. Solo da formato, no decide nada. */
object Formateador {

    /** Español de Chile: decide el separador decimal y el orden de la fecha. */
    private val CHILE: Locale = Locale.forLanguageTag("es-CL")

    private val FECHA_CORTA = SimpleDateFormat("dd/MM/yyyy", CHILE)
    private val FECHA_CON_HORA = SimpleDateFormat("dd/MM/yyyy HH:mm", CHILE)

    fun fecha(instante: Long): String = FECHA_CORTA.format(Date(instante))

    fun fechaYHora(instante: Long): String = FECHA_CON_HORA.format(Date(instante))

    fun puntaje(valor: Double): String = String.format(CHILE, "%.1f", valor)

    fun pesoArchivo(bytes: Long): String = when {
        bytes >= 1024 * 1024 -> String.format(CHILE, "%.1f MB", bytes / 1024.0 / 1024.0)
        bytes >= 1024 -> "${bytes / 1024} KB"
        else -> "$bytes B"
    }

    fun anios(cantidad: Int): String = if (cantidad == 1) "1 año" else "$cantidad años"
}
