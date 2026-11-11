package cl.dymcyber.wim100.core.dominio

/**
 * Perfiles que el equipo priorizo para el MVP, segun el caso WIM 100.
 *
 * El caso menciona cuatro perfiles posibles; aqui se implementan los dos que
 * cierran el flujo completo: quien evalua y quien resuelve.
 */
enum class PerfilUsuario(
    val rotulo: String,
    val resumen: String,
    val exigeVerificacion: Boolean
) {

    EVALUADORA(
        rotulo = "Evaluadora",
        resumen = "Registra postulaciones y las valora con la matriz WIM.",
        exigeVerificacion = false
    ),

    DIRECCION(
        rotulo = "Dirección",
        resumen = "Revisa resultados consolidados y resuelve postulaciones evaluadas.",
        exigeVerificacion = true
    );

    val puedeResolver: Boolean get() = this == DIRECCION
    val puedeValorar: Boolean get() = this == EVALUADORA
}

/**
 * Sesion activa. Cada accion de la app queda firmada con esta identidad,
 * que es lo que da trazabilidad a la bitacora.
 *
 * [credencial] es un identificador estable derivado del nombre: se usa como
 * clave para impedir que la misma evaluadora valore dos veces a una postulante,
 * aunque escriba su nombre con otras mayusculas o espacios.
 */
data class Sesion(
    val nombre: String,
    val perfil: PerfilUsuario
) {
    val credencial: String get() = acreditar(nombre)

    companion object {
        /** "  Ana   Perez " y "ana perez" apuntan a la misma persona. */
        fun acreditar(nombre: String): String =
            nombre.trim().lowercase().replace(Regex("\\s+"), " ")
    }
}
