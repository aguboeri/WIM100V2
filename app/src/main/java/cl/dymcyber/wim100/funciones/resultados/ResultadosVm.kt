package cl.dymcyber.wim100.funciones.resultados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.datos.bd.dao.FilaConsolidada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado del tablero de resultados. */
data class ResultadosUi(
    val filas: List<FilaConsolidada> = emptyList(),
    val cargando: Boolean = true
) {
    val conValoraciones: List<FilaConsolidada> get() = filas.filter { it.cantidadValoraciones > 0 }
    val pendientes: Int get() = filas.count { it.cantidadValoraciones == 0 }
}

/** Resultados consolidados y resolución de Dirección. */
class ResultadosVm(private val deps: Dependencias) : ViewModel() {

    val sesion: StateFlow<Sesion?> = deps.sesion

    val estado: StateFlow<ResultadosUi> = deps.valoraciones.observarConsolidado()
        .map { ResultadosUi(filas = it, cargando = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultadosUi())

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    fun resolver(postulanteId: Long, decision: EtapaPostulacion) {
        val sesionActiva = deps.sesion.value ?: return
        viewModelScope.launch {
            runCatching { deps.postulantes.resolver(postulanteId, decision, sesionActiva) }
                .onSuccess { _aviso.value = "Resolución registrada: ${decision.rotulo}" }
                .onFailure { _aviso.value = it.message ?: "No se pudo registrar la resolución" }
        }
    }

    fun limpiarAviso() { _aviso.value = null }
}
