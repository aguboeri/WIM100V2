package cl.dymcyber.wim100.core.dominio

import kotlin.math.roundToInt

/**
 * Traduce los puntajes de la matriz WIM a una nota unica de 0 a 100.
 *
 * Kotlin puro, sin Android ni Room: por eso se puede cubrir con pruebas
 * unitarias que corren en segundos, sin emulador.
 *
 * Formula: se suma cada puntaje multiplicado por el peso de su criterio y se
 * divide por el maximo alcanzable, que es el peso total por el puntaje maximo.
 */
object PonderadorWim {

    const val PUNTAJE_MINIMO = 1
    const val PUNTAJE_MAXIMO = 5

    /** Un criterio puntuado: cuanto pesa en la matriz y que nota obtuvo. */
    data class Aporte(val peso: Int, val puntaje: Int)

    /**
     * @return puntaje ponderado entre 0.0 y 100.0, con un decimal.
     *         Una lista vacia o con pesos en cero devuelve 0.0.
     */
    fun consolidar(aportes: List<Aporte>): Double {
        val pesoTotal = aportes.sumOf { it.peso }
        if (pesoTotal <= 0) return 0.0

        val obtenido = aportes.sumOf { it.peso * it.puntaje }
        val maximo = pesoTotal * PUNTAJE_MAXIMO
        return redondearAUnDecimal(obtenido * 100.0 / maximo)
    }

    /** Promedio de varias valoraciones de una misma postulante. */
    fun promediar(puntajes: List<Double>): Double =
        if (puntajes.isEmpty()) 0.0 else redondearAUnDecimal(puntajes.average())

    fun nivelDe(puntaje: Double): NivelDesempenio = NivelDesempenio.desde(puntaje)

    private fun redondearAUnDecimal(valor: Double): Double = (valor * 10).roundToInt() / 10.0
}
