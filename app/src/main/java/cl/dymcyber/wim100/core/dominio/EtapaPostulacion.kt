package cl.dymcyber.wim100.core.dominio

/**
 * Etapas por las que avanza una postulacion dentro del programa WIM 100.
 *
 * El avance es de una sola direccion: una postulacion recibida pasa a revision
 * cuando llega su primera valoracion, queda evaluada cuando reune el minimo de
 * valoraciones y solo Direccion puede cerrarla (seleccionada o descartada).
 */
enum class EtapaPostulacion(val rotulo: String) {

    RECIBIDA("Recibida"),
    EN_REVISION("En revisión"),
    EVALUADA("Evaluada"),
    SELECCIONADA("Seleccionada"),
    DESCARTADA("Descartada");

    /** Una postulacion cerrada por Direccion ya no admite nuevas valoraciones. */
    val estaCerrada: Boolean
        get() = this == SELECCIONADA || this == DESCARTADA

    /** Solo se puede resolver lo que ya reunio las valoraciones minimas. */
    val admiteResolucion: Boolean
        get() = this == EVALUADA

    companion object {
        /** Valoraciones necesarias para dar una postulacion por evaluada. */
        const val VALORACIONES_PARA_CERRAR_REVISION = 2

        /**
         * Calcula en que etapa deberia estar una postulacion segun cuantas
         * valoraciones acumula. Las etapas resueltas no se recalculan.
         */
        fun segunValoraciones(actual: EtapaPostulacion, cantidad: Int): EtapaPostulacion = when {
            actual.estaCerrada -> actual
            cantidad >= VALORACIONES_PARA_CERRAR_REVISION -> EVALUADA
            cantidad > 0 -> EN_REVISION
            else -> RECIBIDA
        }
    }
}
