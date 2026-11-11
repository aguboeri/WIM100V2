package cl.dymcyber.wim100.core

import cl.dymcyber.wim100.core.reglas.ReglasValoracion
import cl.dymcyber.wim100.core.reglas.aprueba
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas de la matriz de evaluación. */
class ReglasValoracionTest {

    @Test
    fun `el puntaje solo acepta de uno a cinco`() {
        assertTrue(ReglasValoracion.puntaje(1).aprueba)
        assertTrue(ReglasValoracion.puntaje(5).aprueba)
        assertFalse(ReglasValoracion.puntaje(0).aprueba)
        assertFalse(ReglasValoracion.puntaje(6).aprueba)
        assertFalse(ReglasValoracion.puntaje(null).aprueba)
    }

    @Test
    fun `un puntaje bajo exige justificacion`() {
        assertFalse(ReglasValoracion.justificacion(1, "poco").aprueba)
        assertFalse(ReglasValoracion.justificacion(2, "").aprueba)
        assertTrue(ReglasValoracion.justificacion(2, "No acredita experiencia en el área").aprueba)
    }

    @Test
    fun `un puntaje alto no exige justificacion`() {
        assertTrue(ReglasValoracion.justificacion(3, "").aprueba)
        assertTrue(ReglasValoracion.justificacion(5, "").aprueba)
    }

    @Test
    fun `la observacion general tiene largo minimo y maximo`() {
        assertFalse(ReglasValoracion.cierre("corto").aprueba)
        assertTrue(ReglasValoracion.cierre("Cumple con el perfil buscado para el programa.").aprueba)
        assertFalse(ReglasValoracion.cierre("a".repeat(401)).aprueba)
    }

    @Test
    fun `la matriz solo es valida si los pesos suman cien`() {
        assertTrue(ReglasValoracion.pesosDeLaMatriz(listOf(25, 20, 20, 20, 15)).aprueba)
        assertFalse(ReglasValoracion.pesosDeLaMatriz(listOf(25, 20, 20, 20)).aprueba)
        assertFalse(ReglasValoracion.pesosDeLaMatriz(emptyList()).aprueba)
    }
}
