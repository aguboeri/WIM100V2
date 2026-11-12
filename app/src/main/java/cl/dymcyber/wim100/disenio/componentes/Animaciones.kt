package cl.dymcyber.wim100.disenio.componentes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Sacude el elemento de lado a lado.
 *
 * @param disparador cada vez que este número cambia, la animación vuelve a correr.
 */
@Composable
fun Modifier.sacudir(disparador: Int): Modifier {
    val desplazamiento = remember { Animatable(0f) }

    LaunchedEffect(disparador) {
        if (disparador == 0) return@LaunchedEffect
        repeat(3) {
            desplazamiento.animateTo(10f, tween(durationMillis = 45))
            desplazamiento.animateTo(-10f, tween(durationMillis = 45))
        }
        desplazamiento.animateTo(0f, tween(durationMillis = 45))
    }

    return this.offset(x = desplazamiento.value.dp)
}
