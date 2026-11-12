package cl.dymcyber.wim100.navegacion

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.funciones.acceso.AccesoPantalla
import cl.dymcyber.wim100.funciones.bitacora.BitacoraPantalla
import cl.dymcyber.wim100.funciones.cuenta.CuentaPantalla
import cl.dymcyber.wim100.funciones.postulantes.FichaPantalla
import cl.dymcyber.wim100.funciones.postulantes.FormularioPantalla
import cl.dymcyber.wim100.funciones.postulantes.PostulantesPantalla
import cl.dymcyber.wim100.funciones.resultados.ResultadosPantalla
import cl.dymcyber.wim100.funciones.valoracion.ValoracionPantalla

/** Grafo de navegación. */
@Composable
fun GrafoWim(
    navegador: NavHostController,
    sesion: Sesion?,
    alCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navegador,
        startDestination = Destinos.ACCESO,
        modifier = modifier,
        enterTransition = { slideInHorizontally(tween(260)) { ancho -> ancho / 5 } + fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { slideOutHorizontally(tween(260)) { ancho -> ancho / 5 } + fadeOut(tween(180)) }
    ) {

        composable(Destinos.ACCESO) {
            AccesoPantalla(
                alEntrar = {
                    navegador.navigate(Destinos.POSTULANTES) {
                        // Quita el acceso de la pila: volver atrás no debe
                        // devolver a una sesión ya iniciada.
                        popUpTo(Destinos.ACCESO) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinos.POSTULANTES) {
            PostulantesPantalla(
                alAbrirFicha = { id -> navegador.navigate(Destinos.ficha(id)) }
            )
        }

        composable(Destinos.NUEVA_POSTULACION) {
            FormularioPantalla(alTerminar = { navegador.popBackStack() })
        }

        composable(
            route = Destinos.FICHA,
            arguments = listOf(navArgument(Destinos.ARG_POSTULANTE) { type = NavType.LongType })
        ) {
            FichaPantalla(
                alValorar = { id -> navegador.navigate(Destinos.valorar(id)) }
            )
        }

        composable(
            route = Destinos.VALORAR,
            arguments = listOf(navArgument(Destinos.ARG_POSTULANTE) { type = NavType.LongType })
        ) {
            ValoracionPantalla(alTerminar = { navegador.popBackStack() })
        }

        composable(Destinos.RESULTADOS) {
            ResultadosPantalla(
                alAbrirFicha = { id -> navegador.navigate(Destinos.ficha(id)) }
            )
        }

        composable(Destinos.BITACORA) { BitacoraPantalla() }

        composable(Destinos.CUENTA) {
            CuentaPantalla(sesion = sesion, alCerrarSesion = alCerrarSesion)
        }
    }
}
