package cl.dymcyber.wim100.core.dominio

/** Lectura cualitativa del puntaje ponderado (escala 0 a 100). */
enum class NivelDesempenio(val rotulo: String, val pisoInclusive: Int) {

    SOBRESALIENTE("Sobresaliente", 85),
    SOLIDA("Sólida", 70),
    EN_DESARROLLO("En desarrollo", 50),
    INSUFICIENTE("Insuficiente", 0);

    companion object {
        /** Devuelve el primer nivel cuyo piso alcanza el puntaje recibido. */
        fun desde(puntaje: Double): NivelDesempenio =
            entries.first { puntaje >= it.pisoInclusive }
    }
}
