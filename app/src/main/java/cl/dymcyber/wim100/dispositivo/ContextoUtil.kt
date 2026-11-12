package cl.dymcyber.wim100.dispositivo

import android.content.Context
import android.content.ContextWrapper
import androidx.fragment.app.FragmentActivity

/** Busca la Activity detrás del Context que entrega Compose. */
fun Context.comoActividad(): FragmentActivity? {
    var actual: Context = this
    while (actual is ContextWrapper) {
        if (actual is FragmentActivity) return actual
        actual = actual.baseContext
    }
    return null
}
