package cl.dymcyber.wim100.funciones.postulantes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.core.reglas.ReglasPostulante
import cl.dymcyber.wim100.core.reglas.aprueba
import cl.dymcyber.wim100.core.reglas.aviso
import cl.dymcyber.wim100.datos.acceso.CorreoRepetidoException
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.funciones.Campo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del formulario de postulación. */
data class FormularioUi(
    val nombre: Campo = Campo(),
    val correo: Campo = Campo(),
    val telefono: Campo = Campo(),
    val especialidad: String? = null,
    val reparoEspecialidad: String? = null,
    val trayectoria: Campo = Campo(),
    val sintesis: Campo = Campo(),
    val perfilWeb: Campo = Campo(),
    val cvUri: String? = null,
    val cvNombre: String? = null,
    val reparoCv: String? = null,
    val guardando: Boolean = false,
    val guardado: Boolean = false,
    val avisoGeneral: String? = null,
    val intentosFallidos: Int = 0
) {
    /** Cuántos caracteres lleva la síntesis, para el contador bajo el campo. */
    val largoSintesis: Int get() = sintesis.valor.trim().length
}

/**
 * Formulario de registro de una postulación.
 *
 * Cada tecla dispara la regla del campo correspondiente, así el error aparece
 * mientras se escribe y no al final. Las reglas no están aquí: viven en
 * `ReglasPostulante`, que es Kotlin puro y tiene sus propias pruebas.
 */
class FormularioVm(private val deps: Dependencias) : ViewModel() {

    private val _estado = MutableStateFlow(FormularioUi())
    val estado: StateFlow<FormularioUi> = _estado.asStateFlow()

    fun onNombre(texto: String) = _estado.update {
        it.copy(nombre = it.nombre.revisado(texto, ReglasPostulante.nombre(texto)), avisoGeneral = null)
    }

    fun onCorreo(texto: String) = _estado.update {
        it.copy(correo = it.correo.revisado(texto, ReglasPostulante.correo(texto)), avisoGeneral = null)
    }

    fun onTelefono(texto: String) = _estado.update {
        it.copy(telefono = it.telefono.revisado(texto, ReglasPostulante.telefono(texto)), avisoGeneral = null)
    }

    fun onEspecialidad(valor: String) = _estado.update {
        it.copy(
            especialidad = valor,
            reparoEspecialidad = ReglasPostulante.especialidad(valor).aviso,
            avisoGeneral = null
        )
    }

    fun onTrayectoria(texto: String) = _estado.update {
        val limpio = texto.filter { caracter -> caracter.isDigit() }.take(2)
        it.copy(trayectoria = it.trayectoria.revisado(limpio, ReglasPostulante.trayectoria(limpio)))
    }

    fun onSintesis(texto: String) = _estado.update {
        it.copy(sintesis = it.sintesis.revisado(texto, ReglasPostulante.sintesis(texto)))
    }

    fun onPerfilWeb(texto: String) = _estado.update {
        it.copy(perfilWeb = it.perfilWeb.revisado(texto, ReglasPostulante.perfilWeb(texto)))
    }

    /** Recibe lo que devolvió el selector de documentos del sistema. */
    fun onCurriculum(uri: String?, nombreArchivo: String?, pesoBytes: Long?) = _estado.update {
        val veredicto = ReglasPostulante.curriculum(nombreArchivo, pesoBytes)
        if (veredicto.aprueba) {
            it.copy(cvUri = uri, cvNombre = nombreArchivo, reparoCv = null)
        } else {
            it.copy(cvUri = null, cvNombre = null, reparoCv = veredicto.aviso)
        }
    }

    fun quitarCurriculum() = _estado.update { it.copy(cvUri = null, cvNombre = null, reparoCv = null) }

    /**
     * Revisa todos los campos de una vez, incluidos los que nadie tocó.
     *
     * @return true si el formulario completo está correcto.
     */
    fun revisarTodo(): Boolean {
        val actual = _estado.value
        val nombre = ReglasPostulante.nombre(actual.nombre.valor)
        val correo = ReglasPostulante.correo(actual.correo.valor)
        val telefono = ReglasPostulante.telefono(actual.telefono.valor)
        val especialidad = ReglasPostulante.especialidad(actual.especialidad)
        val trayectoria = ReglasPostulante.trayectoria(actual.trayectoria.valor)
        val sintesis = ReglasPostulante.sintesis(actual.sintesis.valor)
        val perfilWeb = ReglasPostulante.perfilWeb(actual.perfilWeb.valor)

        _estado.update {
            it.copy(
                nombre = it.nombre.revisado(it.nombre.valor, nombre),
                correo = it.correo.revisado(it.correo.valor, correo),
                telefono = it.telefono.revisado(it.telefono.valor, telefono),
                reparoEspecialidad = especialidad.aviso,
                trayectoria = it.trayectoria.revisado(it.trayectoria.valor, trayectoria),
                sintesis = it.sintesis.revisado(it.sintesis.valor, sintesis),
                perfilWeb = it.perfilWeb.revisado(it.perfilWeb.valor, perfilWeb)
            )
        }

        return listOf(nombre, correo, telefono, especialidad, trayectoria, sintesis, perfilWeb)
            .all { it.aprueba }
    }

    /** Guarda la postulación si todo está correcto. */
    fun guardar() {
        if (!revisarTodo()) {
            _estado.update {
                it.copy(
                    intentosFallidos = it.intentosFallidos + 1,
                    avisoGeneral = "Revisa los campos marcados en rojo"
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
                deps.postulantes.registrar(
                    postulante = Postulante(
                        nombre = actual.nombre.valor.trim(),
                        correo = actual.correo.valor.trim().lowercase(),
                        telefono = ReglasPostulante.ordenarTelefono(actual.telefono.valor),
                        especialidad = actual.especialidad.orEmpty(),
                        aniosTrayectoria = actual.trayectoria.valor.trim().toIntOrNull() ?: 0,
                        sintesis = actual.sintesis.valor.trim(),
                        perfilWeb = actual.perfilWeb.valor.trim(),
                        cvUri = actual.cvUri,
                        cvNombre = actual.cvNombre,
                        registradaPor = sesion.nombre
                    ),
                    sesion = sesion
                )
                _estado.update { it.copy(guardando = false, guardado = true) }
            } catch (repetido: CorreoRepetidoException) {
                _estado.update {
                    it.copy(
                        guardando = false,
                        correo = it.correo.copy(reparo = repetido.message),
                        intentosFallidos = it.intentosFallidos + 1,
                        avisoGeneral = repetido.message
                    )
                }
            } catch (error: Exception) {
                _estado.update {
                    it.copy(guardando = false, avisoGeneral = "No se pudo guardar: ${error.message}")
                }
            }
        }
    }
}
