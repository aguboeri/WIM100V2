package cl.dymcyber.wim100.core.dominio

/** Perfiles que el equipo priorizo para el MVP, según el caso WIM 100. */
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

/** Sesion activa. */
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
