package cl.dymcyber.wim100.core

import cl.dymcyber.wim100.core.reglas.ReglasPostulante
import cl.dymcyber.wim100.core.reglas.aprueba
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pruebas de las reglas del formulario de postulación. */
class ReglasPostulanteTest {

    @Test
    fun `el nombre necesita nombre y apellido`() {
        assertTrue(ReglasPostulante.nombre("Ana Pérez").aprueba)
        assertFalse(ReglasPostulante.nombre("Ana").aprueba)
        assertFalse(ReglasPostulante.nombre("").aprueba)
        assertFalse(ReglasPostulante.nombre("Ana P3rez").aprueba)
    }

    @Test
    fun `solo se aceptan correos de dominios de prueba`() {
        assertTrue(ReglasPostulante.correo("ana.perez@ejemplo.cl").aprueba)
        assertTrue(ReglasPostulante.correo("ana@test.cl").aprueba)
        assertFalse(ReglasPostulante.correo("ana@gmail.com").aprueba)
        assertFalse(ReglasPostulante.correo("ana.perez").aprueba)
    }

    @Test
    fun `el telefono acepta las formas chilenas habituales`() {
        assertTrue(ReglasPostulante.telefono("+56 9 1234 5678").aprueba)
        assertTrue(ReglasPostulante.telefono("912345678").aprueba)
        assertFalse(ReglasPostulante.telefono("12345").aprueba)
        assertFalse(ReglasPostulante.telefono("+56 9 1234 567a").aprueba)
    }

    @Test
    fun `el telefono se guarda siempre con el mismo formato`() {
        assertEquals("+56 9 1234 5678", ReglasPostulante.ordenarTelefono("+56912345678"))
        assertEquals("+56 9 1234 5678", ReglasPostulante.ordenarTelefono("9 1234 5678"))
    }

    @Test
    fun `los anios de experiencia deben ser un entero dentro de rango`() {
        assertTrue(ReglasPostulante.trayectoria("7").aprueba)
        assertFalse(ReglasPostulante.trayectoria("siete").aprueba)
        assertFalse(ReglasPostulante.trayectoria("-1").aprueba)
        assertFalse(ReglasPostulante.trayectoria("99").aprueba)
    }

    @Test
    fun `el perfil web debe ser un enlace de prueba con https`() {
        assertTrue(ReglasPostulante.perfilWeb("https://www.linkedin.com/in/ana-ficticia").aprueba)
        assertFalse(ReglasPostulante.perfilWeb("http://www.linkedin.com/in/ana").aprueba)
        assertFalse(ReglasPostulante.perfilWeb("https://instagram.com/ana").aprueba)
    }

    @Test
    fun `el cv debe ser un pdf de hasta cinco megas`() {
        assertTrue(ReglasPostulante.curriculum("cv-prueba.pdf", 1_000_000L).aprueba)
        assertFalse(ReglasPostulante.curriculum("cv-prueba.docx", 1_000L).aprueba)
        assertFalse(ReglasPostulante.curriculum("cv-prueba.pdf", 9_000_000L).aprueba)
        assertFalse(ReglasPostulante.curriculum(null, null).aprueba)
    }
}
