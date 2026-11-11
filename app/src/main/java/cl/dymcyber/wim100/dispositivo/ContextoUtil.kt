package cl.dymcyber.wim100.dispositivo

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

/**
 * Busca la Activity detrás del Context que entrega Compose.
 *
 * `LocalContext.current` no siempre es la Activity: puede venir envuelto. Como
 * BiometricPrompt exige una FragmentActivity, hay que ir desenvolviendo hasta
 * encontrarla.
 */
fun Context.comoActividad(): FragmentActivity? {
    var actual: Context = this
    while (actual is ContextWrapper) {
        if (actual is FragmentActivity) return actual
        actual = actual.baseContext
    }
    return null
}
