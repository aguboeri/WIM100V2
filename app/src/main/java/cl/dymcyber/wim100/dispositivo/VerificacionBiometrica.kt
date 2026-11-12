package cl.dymcyber.wim100.dispositivo

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/** Recurso nativo: verificación biométrica, exigida al perfil Dirección. */
object VerificacionBiometrica {

    /** En qué estado está el equipo respecto de la biometría. */
    enum class Estado { DISPONIBLE, SIN_SENSOR, SIN_REGISTRO, NO_DISPONIBLE }

    private const val METODOS = BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun revisar(contexto: Context): Estado =
        when (BiometricManager.from(contexto).canAuthenticate(METODOS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> Estado.DISPONIBLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Estado.SIN_SENSOR
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Estado.SIN_REGISTRO
            else -> Estado.NO_DISPONIBLE
        }

    /**
     * Muestra el diálogo del sistema.
     *
     * @param alAprobar se llama si la verificación resulta correcta.
     * @param alRechazar recibe el motivo para mostrarlo en pantalla.
     */
    fun pedir(
        actividad: FragmentActivity,
        titulo: String,
        subtitulo: String,
        alAprobar: () -> Unit,
        alRechazar: (String) -> Unit
    ) {
        val prompt = BiometricPrompt(
            actividad,
            ContextCompat.getMainExecutor(actividad),
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(resultado: BiometricPrompt.AuthenticationResult) {
                    alAprobar()
                }

                override fun onAuthenticationError(codigo: Int, mensaje: CharSequence) {
                    alRechazar(mensaje.toString())
                }

                override fun onAuthenticationFailed() {
                    alRechazar("No se reconoció la huella. Inténtalo otra vez.")
                }
            }
        )

        val datos = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle(subtitulo)
            .setAllowedAuthenticators(METODOS)
            .setNegativeButtonText("Cancelar")
            .build()

        prompt.authenticate(datos)
    }
}
