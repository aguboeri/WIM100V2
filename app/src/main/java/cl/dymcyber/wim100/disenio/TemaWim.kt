package cl.dymcyber.wim100.disenio

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Tema Material 3 de la aplicación.
 *
 * A propósito no se usa color dinámico (el que toma los tonos del fondo de
 * pantalla del teléfono): WIM 100 se presenta ante una empresa y la identidad
 * verde tiene que verse igual en cualquier equipo.
 */
private val EsquemaClaro = lightColorScheme(
    primary = VerdeBosque,
    onPrimary = BlancoHueso,
    primaryContainer = VerdeNiebla,
    onPrimaryContainer = VerdeProfundo,
    secondary = VerdeMusgo,
    onSecondary = BlancoHueso,
    secondaryContainer = VerdeContenedor,
    onSecondaryContainer = VerdeProfundo,
    tertiary = CarbonSuave,
    onTertiary = BlancoHueso,
    background = VerdeHumo,
    onBackground = Carbon,
    surface = BlancoHueso,
    onSurface = Carbon,
    surfaceVariant = SuperficieVariante,
    onSurfaceVariant = GrisPiedra,
    outline = GrisBorde,
    outlineVariant = BordeSuave,
    error = RojoReparo,
    onError = BlancoHueso,
    errorContainer = RojoFondo,
    onErrorContainer = VerdeProfundo,
    scrim = Carbon
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaroNoche,
    onPrimary = VerdeProfundo,
    primaryContainer = VerdeBosque,
    onPrimaryContainer = VerdeNiebla,
    secondary = VerdeSalvia,
    onSecondary = VerdeProfundo,
    secondaryContainer = VerdeContenedorNoche,
    onSecondaryContainer = VerdeNiebla,
    tertiary = VerdeSalvia,
    onTertiary = VerdeProfundo,
    background = VerdeFondoNoche,
    onBackground = TextoNoche,
    surface = VerdeSuperficieNoche,
    onSurface = TextoNoche,
    surfaceVariant = SuperficieVarianteNoche,
    onSurfaceVariant = TextoSuaveNoche,
    outline = VerdeBordeNoche,
    outlineVariant = BordeSuaveNoche,
    error = RojoClaroNoche,
    onError = RojoProfundoNoche,
    errorContainer = RojoContenedorNoche,
    onErrorContainer = RojoTextoNoche,
    scrim = Carbon
)

@Composable
fun TemaWim(
    enOscuro: Boolean = isSystemInDarkTheme(),
    contenido: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (enOscuro) EsquemaOscuro else EsquemaClaro,
        typography = TipografiaWim,
        content = contenido
    )
}
