package cl.dymcyber.wim100

import android.content.Context
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.datos.acceso.RepositorioBitacora
import cl.dymcyber.wim100.datos.acceso.RepositorioPostulantes
import cl.dymcyber.wim100.datos.acceso.RepositorioValoraciones
import cl.dymcyber.wim100.datos.bd.BaseWim
import cl.dymcyber.wim100.datos.bd.PrecargaDemo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Contenedor de dependencias, armado a mano.
 *
 * Crea una sola base de datos y un solo repositorio de cada tipo, y se los
 * entrega a los ViewModel. Es inyección de dependencias sin librerías: para un
 * MVP de este tamaño alcanza y se entiende leyendo una pantalla de código.
 */
class Dependencias(contexto: Context) {

    private val base: BaseWim = BaseWim.obtener(contexto)

    val postulantes = RepositorioPostulantes(
        postulanteDao = base.postulantes(),
        criterioDao = base.criterios(),
        bitacoraDao = base.bitacora()
    )

    val valoraciones = RepositorioValoraciones(base)

    val bitacora = RepositorioBitacora(base.bitacora())

    private val _sesion = MutableStateFlow<Sesion?>(null)

    /** Sesión activa. Null significa que nadie ha ingresado todavía. */
    val sesion: StateFlow<Sesion?> = _sesion.asStateFlow()

    fun abrirSesion(nueva: Sesion) { _sesion.value = nueva }

    fun cerrarSesion() { _sesion.value = null }

    /** Carga los datos de muestra la primera vez que se abre la app. */
    suspend fun prepararDatosDeMuestra() = PrecargaDemo.sembrar(base)
}
