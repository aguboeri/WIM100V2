package cl.dymcyber.wim100.core

import cl.dymcyber.wim100.core.dominio.NivelDesempenio
import cl.dymcyber.wim100.core.dominio.PonderadorWim
import cl.dymcyber.wim100.core.dominio.PonderadorWim.Aporte
import org.junit.Assert.assertEquals
import org.junit.Test

/** Pruebas del cálculo ponderado. */
class PonderadorWimTest {

    /** Los 5 criterios de la matriz de ejemplo con sus pesos. */
    private val pesos = listOf(25, 20, 20, 20, 15)

    private fun matrizCon(puntaje: Int) = pesos.map { Aporte(peso = it, puntaje = puntaje) }

    @Test
    fun `todo en el maximo da cien`() {
        assertEquals(100.0, PonderadorWim.consolidar(matrizCon(5)), 0.01)
    }

    @Test
    fun `todo en el minimo da veinte`() {
        assertEquals(20.0, PonderadorWim.consolidar(matrizCon(1)), 0.01)
    }

    @Test
    fun `una matriz sin criterios no revienta y da cero`() {
        assertEquals(0.0, PonderadorWim.consolidar(emptyList()), 0.01)
    }

    @Test
    fun `el peso de cada criterio cambia el resultado`() {
        val fuerteEnLoQueMasPesa = listOf(
            Aporte(25, 5), Aporte(20, 2), Aporte(20, 2), Aporte(20, 2), Aporte(15, 2)
        )
        val fuerteEnLoQueMenosPesa = listOf(
            Aporte(25, 2), Aporte(20, 2), Aporte(20, 2), Aporte(20, 2), Aporte(15, 5)
        )
        val conMasPeso = PonderadorWim.consolidar(fuerteEnLoQueMasPesa)
        val conMenosPeso = PonderadorWim.consolidar(fuerteEnLoQueMenosPesa)
        assertEquals(true, conMasPeso > conMenosPeso)
    }

    @Test
    fun `el promedio de varias valoraciones se redondea a un decimal`() {
        assertEquals(77.5, PonderadorWim.promediar(listOf(80.0, 75.0)), 0.01)
    }

    @Test
    fun `los cortes de nivel caen donde corresponde`() {
        assertEquals(NivelDesempenio.SOBRESALIENTE, PonderadorWim.nivelDe(85.0))
        assertEquals(NivelDesempenio.SOLIDA, PonderadorWim.nivelDe(84.9))
        assertEquals(NivelDesempenio.EN_DESARROLLO, PonderadorWim.nivelDe(50.0))
        assertEquals(NivelDesempenio.INSUFICIENTE, PonderadorWim.nivelDe(49.9))
    }
}
