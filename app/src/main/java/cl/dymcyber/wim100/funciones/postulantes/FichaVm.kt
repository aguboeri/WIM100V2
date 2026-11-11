package cl.dymcyber.wim100.funciones.postulantes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.datos.bd.entidades.DetalleValoracion
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.datos.bd.entidades.Valoracion
import cl.dymcyber.wim100.navegacion.Destinos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la ficha de una postulante. */
data class FichaUi(
    val postulante: Postulante? = null,
    val valoraciones: List<Valoracion> = emptyList(),
    val eventos: List<EventoBitacora> = emptyList(),
    val cargando: Boolean = true
) {
    val cantidadValoraciones: Int get() = valoraciones.size

    val promedio: Double
        get() = if (valoraciones.isEmpty()) 0.0 else valoraciones.map { it.puntajeTotal }.average()

    fun yaValoro(credencial: String): Boolean =
        valoraciones.any { it.credencialEvaluadora == credencial }
}

/**
 * Ficha completa de una postulante: antecedentes, valoraciones recibidas,
 * acciones disponibles según el perfil y su rastro en la bitácora.
 */
class FichaVm(
    guardado: SavedStateHandle,
    private val deps: Dependencias
) : ViewModel() {

    /** El id llega como argumento de navegación, no por parámetro del constructor. */
    private val postulanteId: Long = guardado.get<Long>(Destinos.ARG_POSTULANTE) ?: 0L

    val sesion: StateFlow<Sesion?> = deps.sesion

    val estado: StateFlow<FichaUi> = combine(
        deps.postulantes.observarUna(postulanteId),
        deps.valoraciones.observarDePostulante(postulanteId),
        deps.bitacora.observarDePostulante(postulanteId)
    ) { postulante, valoraciones, eventos ->
        FichaUi(postulante, valoraciones, eventos, cargando = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FichaUi())

    private val _detalles = MutableStateFlow<Map<Long, List<DetalleValoracion>>>(emptyMap())

    /** Detalle por criterio de cada valoración, cargado al desplegar la tarjeta. */
    val detalles: StateFlow<Map<Long, List<DetalleValoracion>>> = _detalles.asStateFlow()

    /** Nombre de cada criterio, para rotular el detalle sin mostrar números de id. */
    val nombresCriterios: StateFlow<Map<Long, String>> =
        deps.postulantes.observarMatriz()
            .map { matriz -> matriz.associate { it.id to it.nombre } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    fun cargarDetalle(valoracionId: Long) {
        if (_detalles.value.containsKey(valoracionId)) return
        viewModelScope.launch {
            val filas = deps.valoraciones.leerDetalles(valoracionId)
            _detalles.update { it + (valoracionId to filas) }
        }
    }

    /** Guarda el CV elegido con el selector de documentos del sistema. */
    fun adjuntarCurriculum(uri: String?, nombreArchivo: String?) {
        val sesion = deps.sesion.value ?: return
        viewModelScope.launch {
            deps.postulantes.adjuntarCurriculum(postulanteId, uri, nombreArchivo, sesion)
            _aviso.value = if (uri == null) "Se quitó el CV" else "CV adjuntado"
        }
    }

    /** Solo Dirección, y solo sobre una postulación ya evaluada. */
    fun resolver(decision: EtapaPostulacion) {
        val sesion = deps.sesion.value ?: return
        viewModelScope.launch {
            runCatching { deps.postulantes.resolver(postulanteId, decision, sesion) }
                .onSuccess { _aviso.value = "Resolución registrada: ${decision.rotulo}" }
                .onFailure { _aviso.value = it.message }
        }
    }

    fun limpiarAviso() { _aviso.value = null }
}
