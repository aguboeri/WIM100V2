package cl.dymcyber.wim100.funciones.valoracion

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.dymcyber.wim100.core.dominio.PonderadorWim
import cl.dymcyber.wim100.core.reglas.ReglasValoracion
import cl.dymcyber.wim100.datos.bd.entidades.CriterioWim
import cl.dymcyber.wim100.disenio.componentes.MedidorPuntaje
import cl.dymcyber.wim100.disenio.componentes.TituloSeccion
import cl.dymcyber.wim100.disenio.componentes.sacudir
import cl.dymcyber.wim100.dispositivo.AvisosLocales
import cl.dymcyber.wim100.funciones.FabricaVm

/**
 * Aplicación de la matriz WIM.
 *
 * Cada criterio se puntúa de 1 a 5. Si el puntaje es bajo aparece el campo de
 * justificación, obligatorio: una nota baja sin explicación no se puede
 * guardar. El puntaje proyectado se actualiza en vivo para que la evaluadora
 * vea el efecto del peso de cada criterio.
 */
@Composable
fun ValoracionPantalla(
    alTerminar: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ValoracionVm = viewModel(factory = FabricaVm.Reglas)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Recurso nativo: notificaciones. El permiso se pide solo en Android 13+.
    val permisoAvisos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Si lo rechaza, la app sigue funcionando igual. */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !AvisosLocales.permitido(contexto)) {
            permisoAvisos.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(estado.guardado) {
        if (!estado.guardado) return@LaunchedEffect
        AvisosLocales.avisarValoracion(
            contexto = contexto,
            nombrePostulante = estado.postulante?.nombre.orEmpty(),
            puntaje = estado.puntajeLogrado
        )
        alTerminar()
    }

    val avance by animateFloatAsState(estado.avance, tween(400), label = "avance de la matriz")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = estado.postulante?.nombre ?: "Cargando…",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${estado.criteriosCompletos} de ${estado.matriz.size} criterios puntuados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { avance },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.width(12.dp))
                MedidorPuntaje(puntaje = estado.proyeccion, diametro = 78)
            }
        }

        TituloSeccion(
            texto = "Matriz de evaluación",
            apoyo = "Un puntaje de ${ReglasValoracion.PUNTAJE_QUE_EXIGE_JUSTIFICACION} o menos exige justificación escrita."
        )

        estado.matriz.forEach { criterio ->
            BloqueCriterio(
                criterio = criterio,
                puntaje = estado.puntajes[criterio.id],
                observacion = estado.observaciones[criterio.id].orEmpty(),
                reparo = estado.reparos[criterio.id],
                alPuntuar = { vm.onPuntaje(criterio.id, it) },
                alEscribir = { vm.onObservacion(criterio.id, it) }
            )
        }

        TituloSeccion(texto = "Observación general")
        OutlinedTextField(
            value = estado.cierre,
            onValueChange = vm::onCierre,
            label = { Text("Conclusión de la evaluación") },
            isError = estado.reparoCierre != null,
            supportingText = {
                Text(
                    text = estado.reparoCierre
                        ?: "${estado.cierre.trim().length}/${ReglasValoracion.CIERRE_LARGO_MAXIMO} caracteres",
                    color = if (estado.reparoCierre != null) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = if (estado.reparoCierre != null) {
                { Icon(Icons.Filled.ErrorOutline, contentDescription = "Campo con error", tint = MaterialTheme.colorScheme.error) }
            } else null,
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = estado.avisoGeneral != null, enter = fadeIn(), exit = fadeOut()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = estado.avisoGeneral.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Button(
            onClick = vm::guardar,
            enabled = !estado.guardando,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .sacudir(estado.intentosFallidos)
        ) {
            if (estado.guardando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(10.dp))
                Text("Guardando…")
            } else {
                Text("Registrar valoración")
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

/** Un criterio de la matriz: su peso, los cinco puntajes y la justificación. */
@Composable
private fun BloqueCriterio(
    criterio: CriterioWim,
    puntaje: Int?,
    observacion: String,
    reparo: String?,
    alPuntuar: (Int) -> Unit,
    alEscribir: (String) -> Unit
) {
    val exigeJustificacion = puntaje != null && puntaje <= ReglasValoracion.PUNTAJE_QUE_EXIGE_JUSTIFICACION

    Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = criterio.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "peso ${criterio.peso}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = criterio.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                (PonderadorWim.PUNTAJE_MINIMO..PonderadorWim.PUNTAJE_MAXIMO).forEach { valor ->
                    if (puntaje == valor) {
                        Button(onClick = { alPuntuar(valor) }, modifier = Modifier.weight(1f)) {
                            Text("$valor")
                        }
                    } else {
                        FilledTonalButton(onClick = { alPuntuar(valor) }, modifier = Modifier.weight(1f)) {
                            Text("$valor")
                        }
                    }
                }
            }

            AnimatedVisibility(visible = exigeJustificacion) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    OutlinedTextField(
                        value = observacion,
                        onValueChange = alEscribir,
                        label = { Text("Justificación obligatoria") },
                        isError = reparo != null,
                        supportingText = {
                            Text(
                                text = reparo ?: "Explica brevemente el puntaje bajo",
                                color = if (reparo != null) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!exigeJustificacion && reparo != null) {
                Text(
                    text = reparo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
