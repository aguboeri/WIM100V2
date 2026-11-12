package cl.dymcyber.wim100.funciones.bitacora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Estado de la bitácora. */
data class BitacoraUi(
    val eventos: List<EventoBitacora> = emptyList(),
    val total: Int = 0,
    val cargando: Boolean = true
)

/** Bitácora de acciones, con búsqueda por texto. */
class BitacoraVm(deps: Dependencias) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val estado: StateFlow<BitacoraUi> =
        combine(deps.bitacora.observarRecientes(), _busqueda) { eventos, texto ->
            val buscado = texto.trim().lowercase()
            val filtrados = if (buscado.isEmpty()) eventos else eventos.filter {
                it.accion.lowercase().contains(buscado) ||
                    it.detalle.lowercase().contains(buscado) ||
                    it.autor.lowercase().contains(buscado)
            }
            BitacoraUi(eventos = filtrados, total = eventos.size, cargando = false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BitacoraUi())

    fun onBuscar(texto: String) { _busqueda.value = texto }
}
