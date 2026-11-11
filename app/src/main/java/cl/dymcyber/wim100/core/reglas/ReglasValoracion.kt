package cl.dymcyber.wim100.core.reglas

import cl.dymcyber.wim100.core.dominio.PonderadorWim

/**
 * Reglas de la matriz de evaluacion WIM.
 *
 * La regla de fondo: una nota baja no se puede dejar sin explicacion. Si un
 * criterio recibe 1 o 2, la evaluadora tiene que justificarlo por escrito, para
 * que la postulante y Direccion puedan entender la decision despues.
 */
object ReglasValoracion {

    /** Desde este puntaje hacia abajo la observacion del criterio es obligatoria. */
    const val PUNTAJE_QUE_EXIGE_JUSTIFICACION = 2
    const val JUSTIFICACION_LARGO_MINIMO = 10
    const val CIERRE_LARGO_MINIMO = 20
    const val CIERRE_LARGO_MAXIMO = 400

    fun puntaje(valor: Int?): Veredicto {
        if (valor == null) return reparo("Falta puntuar este criterio")
        val rango = PonderadorWim.PUNTAJE_MINIMO..PonderadorWim.PUNTAJE_MAXIMO
        if (valor !in rango) return reparo("El puntaje va de ${rango.first} a ${rango.last}")
        return Veredicto.Correcto
    }

    /** La observacion de un criterio solo es obligatoria cuando el puntaje es bajo. */
    fun justificacion(puntaje: Int?, observacion: String): Veredicto {
        if (puntaje == null || puntaje > PUNTAJE_QUE_EXIGE_JUSTIFICACION) return Veredicto.Correcto
        val texto = observacion.trim()
        if (texto.length < JUSTIFICACION_LARGO_MINIMO) {
            return reparo("Un puntaje de $puntaje necesita al menos $JUSTIFICACION_LARGO_MINIMO caracteres de justificación")
        }
        return Veredicto.Correcto
    }

    fun cierre(entrada: String): Veredicto {
        val texto = entrada.trim()
        if (texto.isEmpty()) return reparo("Escribe la observación general")
        if (texto.length < CIERRE_LARGO_MINIMO) return reparo("Mínimo $CIERRE_LARGO_MINIMO caracteres")
        if (texto.length > CIERRE_LARGO_MAXIMO) return reparo("Máximo $CIERRE_LARGO_MAXIMO caracteres")
        return Veredicto.Correcto
    }

    /** La matriz solo es valida si los pesos suman 100. */
    fun pesosDeLaMatriz(pesos: List<Int>): Veredicto {
        if (pesos.isEmpty()) return reparo("La matriz no tiene criterios cargados")
        val suma = pesos.sum()
        if (suma != 100) return reparo("Los pesos suman $suma y deben sumar 100")
        return Veredicto.Correcto
    }
}
