package cl.dymcyber.wim100.core.dominio

/** Etapas por las que avanza una postulación dentro del programa WIM 100. */
enum class EtapaPostulacion(val rotulo: String) {

    RECIBIDA("Recibida"),
    EN_REVISION("En revisión"),
    EVALUADA("Evaluada"),
    SELECCIONADA("Seleccionada"),
    DESCARTADA("Descartada");

    /** Una postulación cerrada por Dirección ya no admite nuevas valoraciones. */
    val estaCerrada: Boolean
        get() = this == SELECCIONADA || this == DESCARTADA

    /** Solo se puede resolver lo que ya reunió las valoraciones mínimas. */
    val admiteResolucion: Boolean
        get() = this == EVALUADA

    companion object {
        /** Valoraciones necesarias para dar una postulación por evaluada. */
        const val VALORACIONES_PARA_CERRAR_REVISION = 2

        /** Calcula en que etapa debería estar una postulación según cuántas valoraciones acumula. */
        fun segunValoraciones(actual: EtapaPostulacion, cantidad: Int): EtapaPostulacion = when {
            actual.estaCerrada -> actual
            cantidad >= VALORACIONES_PARA_CERRAR_REVISION -> EVALUADA
            cantidad > 0 -> EN_REVISION
            else -> RECIBIDA
        }
    }
}
