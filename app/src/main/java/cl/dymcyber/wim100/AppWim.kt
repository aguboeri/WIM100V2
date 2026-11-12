@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.dymcyber.wim100

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.dymcyber.wim100.navegacion.Destinos
import cl.dymcyber.wim100.navegacion.GrafoWim
import cl.dymcyber.wim100.navegacion.SeccionPrincipal

/** Estructura visual de la aplicación. */
@Composable
fun AppWim() {
    val navegador = rememberNavController()
    val contexto = LocalContext.current
    val dependencias = (contexto.applicationContext as AplicacionWim).dependencias
    val sesion by dependencias.sesion.collectAsStateWithLifecycle()

    val entrada by navegador.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route

    val enAcceso = ruta == Destinos.ACCESO || ruta == null
    val seccion = SeccionPrincipal.entries.firstOrNull { it.ruta == ruta }
    val puedeVolver = !enAcceso && seccion == null

    Scaffold(
        topBar = {
            if (!enAcceso) {
                TopAppBar(
                    title = { Text(tituloDe(ruta)) },
                    navigationIcon = {
                        if (puedeVolver) {
                            IconButton(onClick = { navegador.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver"
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = seccion != null,
                enter = slideInVertically { alto -> alto } + fadeIn(),
                exit = slideOutVertically { alto -> alto } + fadeOut()
            ) {
                NavigationBar {
                    SeccionPrincipal.entries.forEach { destino ->
                        NavigationBarItem(
                            selected = seccion == destino,
                            onClick = {
                                if (seccion == destino) return@NavigationBarItem
                                navegador.navigate(destino.ruta) {
                                    // Evita apilar copias de la misma sección al
                                    // ir y venir por la barra inferior.
                                    popUpTo(Destinos.POSTULANTES) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destino.icono, contentDescription = destino.rotulo) },
                            label = { Text(destino.rotulo) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val puedeRegistrar = sesion?.perfil?.puedeValorar == true
            AnimatedVisibility(
                visible = ruta == Destinos.POSTULANTES && puedeRegistrar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ExtendedFloatingActionButton(
                    onClick = { navegador.navigate(Destinos.NUEVA_POSTULACION) },
                    icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                    text = { Text("Nueva postulación") }
                )
            }
        }
    ) { relleno ->
        GrafoWim(
            navegador = navegador,
            sesion = sesion,
            alCerrarSesion = {
                dependencias.cerrarSesion()
                navegador.navigate(Destinos.ACCESO) {
                    // Limpia toda la pila: tras cerrar sesión no se puede
                    // volver atrás a una pantalla con datos.
                    popUpTo(Destinos.POSTULANTES) { inclusive = true }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
        )
    }
}

/** Título de la barra superior según la ruta activa. */
private fun tituloDe(ruta: String?): String = when (ruta) {
    Destinos.POSTULANTES -> "Postulantes"
    Destinos.RESULTADOS -> "Resultados"
    Destinos.BITACORA -> "Bitácora"
    Destinos.CUENTA -> "Cuenta"
    Destinos.NUEVA_POSTULACION -> "Nueva postulación"
    Destinos.FICHA -> "Ficha de la postulante"
    Destinos.VALORAR -> "Matriz de evaluación"
    else -> "WIM 100"
}
