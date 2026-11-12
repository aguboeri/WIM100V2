package cl.dymcyber.wim100

import android.app.Application
import cl.dymcyber.wim100.dispositivo.AvisosLocales
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Punto de entrada de la aplicación. */
class AplicacionWim : Application() {

    lateinit var dependencias: Dependencias
        private set

    private val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        dependencias = Dependencias(this)
        AvisosLocales.prepararCanal(this)
        alcance.launch { dependencias.prepararDatosDeMuestra() }
    }
}
