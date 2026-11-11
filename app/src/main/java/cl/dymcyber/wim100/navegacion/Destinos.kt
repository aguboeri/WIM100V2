package cl.dymcyber.wim100.navegacion

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Las direcciones de la app, escritas en un solo lugar.
 *
 * Tener las rutas como constantes evita el clásico error de escribir mal un
 * texto en una pantalla y descubrirlo recién al ejecutar.
 */
object Destinos {

    const val ARG_POSTULANTE = "postulanteId"

    const val ACCESO = "acceso"
    const val POSTULANTES = "postulantes"
    const val RESULTADOS = "resultados"
    const val BITACORA = "bitacora"
    const val CUENTA = "cuenta"
    const val NUEVA_POSTULACION = "postulacion/nueva"

    const val FICHA = "postulante/{$ARG_POSTULANTE}"
    const val VALORAR = "valorar/{$ARG_POSTULANTE}"

    fun ficha(id: Long) = "postulante/$id"
    fun valorar(id: Long) = "valorar/$id"
}

/** Las cuatro secciones de la barra inferior. */
enum class SeccionPrincipal(
    val ruta: String,
    val rotulo: String,
    val icono: ImageVector
) {
    POSTULANTES(Destinos.POSTULANTES, "Postulantes", Icons.Filled.Groups),
    RESULTADOS(Destinos.RESULTADOS, "Resultados", Icons.Filled.Insights),
    BITACORA(Destinos.BITACORA, "Bitácora", Icons.Filled.History),
    CUENTA(Destinos.CUENTA, "Cuenta", Icons.Filled.AccountCircle)
}
