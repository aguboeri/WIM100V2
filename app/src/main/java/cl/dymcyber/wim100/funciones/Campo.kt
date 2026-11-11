package cl.dymcyber.wim100.funciones

import cl.dymcyber.wim100.core.reglas.Veredicto
import cl.dymcyber.wim100.core.reglas.aviso

/**
 * Un campo de formulario visto desde el ViewModel: lo que la usuaria escribió y
 * el reparo pendiente, si lo hay.
 *
 * La pantalla no decide nada sobre el error: solo pinta lo que encuentra aquí.
 */
data class Campo(
    val valor: String = "",
    val reparo: String? = null,
    val tocado: Boolean = false
) {
    val correcto: Boolean get() = reparo == null

    /** Devuelve una copia con el valor nuevo y el resultado de revisarlo. */
    fun revisado(nuevo: String, veredicto: Veredicto): Campo =
        Campo(valor = nuevo, reparo = veredicto.aviso, tocado = true)
}
