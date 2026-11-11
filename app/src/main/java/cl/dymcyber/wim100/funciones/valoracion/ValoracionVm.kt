package cl.dymcyber.wim100.funciones.valoracion

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.core.dominio.PonderadorWim
import cl.dymcyber.wim100.core.reglas.ReglasValoracion
import cl.dymcyber.wim100.core.reglas.aprueba
import cl.dymcyber.wim100.core.reglas.aviso
import cl.dymcyber.wim100.datos.acceso.PostulacionCerradaException
import cl.dymcyber.wim100.datos.acceso.ValoracionDuplicadaException
import cl.dymcyber.wim100.datos.bd.entidades.CriterioWim
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.navegacion.Destinos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla de evaluación. */
data class ValoracionUi(
    val postulante: Postulante? = null,
    val matriz: List<CriterioWim> = emptyList(),
    val puntajes: Map<Long, Int> = emptyMap(),
    val observaciones: Map<Long, String> = emptyMap(),
    val reparos: Map<Long, String> = emptyMap(),
    val cierre: String = "",
    val reparoCierre: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val puntajeLogrado: Double = 0.0,
    val avisoGeneral: String? = null,
    val intentosFallidos: Int = 0,
    val cargando: Boolean = true
) {
    val criteriosCompletos: Int get() = matriz.count { puntajes.containsKey(it.id) }

    /** Avance de 0 a 1 para la barra de progreso. */
    val avance: Float
        get() = if (matriz.isEmpty()) 0f else criteriosCompletos.toFloat() / matriz.size

    /** Puntaje proyectado con lo que lleva puntuado hasta ahora. */
    val proyeccion: Double
        get() = PonderadorWim.consolidar(
            matriz.filter { puntajes.containsKey(it.id) }
                .map { PonderadorWim.Aporte(it.peso, puntajes.getValue(it.id)) }
        )
}

/**
 * Aplicación de la matriz WIM a una postulante.
 *
 * La regla que gobierna esta pantalla: un puntaje bajo exige justificación. El
 * ViewModel la revisa en cuanto cambia el puntaje o el texto, para que la
 * evaluadora vea el requisito apenas aparece y no al apretar Guardar.
 */
class ValoracionVm(
    guardado: SavedStateHandle,
    private val deps: Dependencias
) : ViewModel() {

    private val postulanteId: Long = guardado.get<Long>(Destinos.ARG_POSTULANTE) ?: 0L

    private val _estado = MutableStateFlow(ValoracionUi())
    val estado: StateFlow<ValoracionUi> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val postulante = deps.postulantes.buscarPorId(postulanteId)
            val matriz = deps.postulantes.observarMatriz().first()
            _estado.update {
                it.copy(postulante = postulante, matriz = matriz, cargando = false)
            }
        }
    }

    fun onPuntaje(criterioId: Long, valor: Int) = _estado.update { actual ->
        val puntajes = actual.puntajes + (criterioId to valor)
        val observacion = actual.observaciones[criterioId].orEmpty()
        actual.copy(
            puntajes = puntajes,
            reparos = conReparo(actual.reparos, criterioId, valor, observacion),
            avisoGeneral = null
        )
    }

    fun onObservacion(criterioId: Long, texto: String) = _estado.update { actual ->
        val observaciones = actual.observaciones + (criterioId to texto)
        actual.copy(
            observaciones = observaciones,
            reparos = conReparo(actual.reparos, criterioId, actual.puntajes[criterioId], texto),
            avisoGeneral = null
        )
    }

    fun onCierre(texto: String) = _estado.update {
        it.copy(cierre = texto, reparoCierre = ReglasValoracion.cierre(texto).aviso, avisoGeneral = null)
    }

    /** Aplica la regla de justificación obligatoria a un criterio. */
    private fun conReparo(
        actuales: Map<Long, String>,
        criterioId: Long,
        puntaje: Int?,
        observacion: String
    ): Map<Long, String> {
        val veredicto = ReglasValoracion.justificacion(puntaje, observacion)
        return if (veredicto.aprueba) actuales - criterioId
        else actuales + (criterioId to veredicto.aviso.orEmpty())
    }

    /** Revisa la matriz completa: todos los criterios puntuados y justificados. */
    private fun revisarTodo(): Boolean {
        val actual = _estado.value
        val reparos = mutableMapOf<String, String>()
        var reparosPorCriterio = emptyMap<Long, String>()

        actual.matriz.forEach { criterio ->
            val puntaje = actual.puntajes[criterio.id]
            val revisionPuntaje = ReglasValoracion.puntaje(puntaje)
            if (!revisionPuntaje.aprueba) {
                reparosPorCriterio = reparosPorCriterio + (criterio.id to revisionPuntaje.aviso.orEmpty())
                reparos[criterio.nombre] = revisionPuntaje.aviso.orEmpty()
                return@forEach
            }
            val revisionTexto = ReglasValoracion.justificacion(
                puntaje,
                actual.observaciones[criterio.id].orEmpty()
            )
            if (!revisionTexto.aprueba) {
                reparosPorCriterio = reparosPorCriterio + (criterio.id to revisionTexto.aviso.orEmpty())
                reparos[criterio.nombre] = revisionTexto.aviso.orEmpty()
            }
        }

        val revisionCierre = ReglasValoracion.cierre(actual.cierre)

        _estado.update {
            it.copy(reparos = reparosPorCriterio, reparoCierre = revisionCierre.aviso)
        }
        return reparos.isEmpty() && revisionCierre.aprueba
    }

    /** Envía la valoración al repositorio, que la guarda en una transacción. */
    fun guardar() {
        if (!revisarTodo()) {
            _estado.update {
                it.copy(
                    intentosFallidos = it.intentosFallidos + 1,
                    avisoGeneral = "Faltan puntajes o justificaciones"
                )
            }
            return
        }

        val sesion = deps.sesion.value
        if (sesion == null) {
            _estado.update { it.copy(avisoGeneral = "La sesión se cerró. Vuelve a ingresar.") }
            return
        }

        val actual = _estado.value
        _estado.update { it.copy(guardando = true, avisoGeneral = null) }

        viewModelScope.launch {
            try {
                val total = deps.valoraciones.registrar(
                    postulanteId = postulanteId,
                    sesion = sesion,
                    matriz = actual.matriz,
                    puntajesPorCriterio = actual.puntajes,
                    observacionesPorCriterio = actual.observaciones,
                    cierre = actual.cierre
                )
                _estado.update { it.copy(guardando = false, guardado = true, puntajeLogrado = total) }
            } catch (duplicada: ValoracionDuplicadaException) {
                _estado.update {
                    it.copy(guardando = false, avisoGeneral = duplicada.message, intentosFallidos = it.intentosFallidos + 1)
                }
            } catch (cerrada: PostulacionCerradaException) {
                _estado.update { it.copy(guardando = false, avisoGeneral = cerrada.message) }
            } catch (error: Exception) {
                _estado.update { it.copy(guardando = false, avisoGeneral = "No se pudo guardar: ${error.message}") }
            }
        }
    }
}
