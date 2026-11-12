package cl.dymcyber.wim100.funciones.postulantes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Estado de la lista de postulaciones. */
data class ListaUi(
    val visibles: List<Postulante> = emptyList(),
    val total: Int = 0,
    val cargando: Boolean = true
) {
    val vacioPorFiltro: Boolean get() = !cargando && visibles.isEmpty() && total > 0
    val sinDatos: Boolean get() = !cargando && total == 0
}

/** Lista de postulaciones con búsqueda y filtro por etapa. */
class ListaVm(deps: Dependencias) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    private val _etapa = MutableStateFlow<EtapaPostulacion?>(null)
    val etapa: StateFlow<EtapaPostulacion?> = _etapa.asStateFlow()

    val estado: StateFlow<ListaUi> =
        combine(deps.postulantes.observarTodas(), _busqueda, _etapa) { lista, texto, etapaElegida ->
            val buscado = texto.trim().lowercase()
            val filtrada = lista.filter { postulante ->
                val calzaTexto = buscado.isEmpty() ||
                    postulante.nombre.lowercase().contains(buscado) ||
                    postulante.especialidad.lowercase().contains(buscado) ||
                    postulante.correo.lowercase().contains(buscado)
                val calzaEtapa = etapaElegida == null || postulante.etapa == etapaElegida
                calzaTexto && calzaEtapa
            }
            ListaUi(visibles = filtrada, total = lista.size, cargando = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListaUi()
        )

    fun onBuscar(texto: String) { _busqueda.value = texto }

    fun onEtapa(nueva: EtapaPostulacion?) { _etapa.value = if (_etapa.value == nueva) null else nueva }

    fun limpiarFiltros() {
        _busqueda.value = ""
        _etapa.value = null
    }
}
