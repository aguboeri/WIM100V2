package cl.dymcyber.wim100.funciones.acceso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.core.dominio.PerfilUsuario
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.core.reglas.ReglasPostulante
import cl.dymcyber.wim100.core.reglas.aprueba
import cl.dymcyber.wim100.funciones.Campo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla de ingreso. */
data class AccesoUi(
    val nombre: Campo = Campo(),
    val perfil: PerfilUsuario = PerfilUsuario.EVALUADORA,
    val verificando: Boolean = false,
    val aviso: String? = null
) {
    val listoParaEntrar: Boolean get() = nombre.valor.isNotBlank() && nombre.correcto
}

/** Identifica a quien usa la app. */
class AccesoVm(private val deps: Dependencias) : ViewModel() {

    private val _estado = MutableStateFlow(AccesoUi())
    val estado: StateFlow<AccesoUi> = _estado.asStateFlow()

    fun onNombre(texto: String) = _estado.update {
        it.copy(nombre = it.nombre.revisado(texto, ReglasPostulante.nombre(texto)), aviso = null)
    }

    fun onPerfil(perfil: PerfilUsuario) = _estado.update { it.copy(perfil = perfil, aviso = null) }

    fun marcandoVerificacion(enCurso: Boolean) = _estado.update { it.copy(verificando = enCurso) }

    fun avisar(texto: String?) = _estado.update { it.copy(aviso = texto, verificando = false) }

    /** Revisa el formulario completo antes de pedir la huella. */
    fun formularioValido(): Boolean {
        val veredicto = ReglasPostulante.nombre(_estado.value.nombre.valor)
        _estado.update { it.copy(nombre = it.nombre.revisado(it.nombre.valor, veredicto)) }
        return veredicto.aprueba
    }

    /** Abre la sesión y deja el ingreso anotado en la bitácora. */
    fun entrar(alTerminar: () -> Unit) {
        val actual = _estado.value
        if (!formularioValido()) return
        val sesion = Sesion(nombre = actual.nombre.valor.trim(), perfil = actual.perfil)
        deps.abrirSesion(sesion)
        viewModelScope.launch { deps.bitacora.anotarIngreso(sesion) }
        _estado.update { it.copy(verificando = false) }
        alTerminar()
    }
}
