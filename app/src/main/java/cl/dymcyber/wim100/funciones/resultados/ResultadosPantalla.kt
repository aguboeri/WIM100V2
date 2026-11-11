@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.dymcyber.wim100.funciones.resultados

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.dominio.NivelDesempenio
import cl.dymcyber.wim100.core.formato.Formateador
import cl.dymcyber.wim100.datos.bd.dao.FilaConsolidada
import cl.dymcyber.wim100.disenio.componentes.EstadoVacio
import cl.dymcyber.wim100.disenio.componentes.EtiquetaEtapa
import cl.dymcyber.wim100.funciones.FabricaVm

/**
 * Resultados consolidados y resolución de Dirección.
 *
 * El orden lo calcula SQLite con un promedio agrupado, no la pantalla. Los
 * botones de resolver solo aparecen para el perfil Dirección y solo sobre
 * postulaciones que ya reunieron las valoraciones mínimas.
 */
@Composable
fun ResultadosPantalla(
    alAbrirFicha: (Long) -> Unit,
    modifier: Modifier = Modifier,
    vm: ResultadosVm = viewModel(factory = FabricaVm.Reglas)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val esDireccion = sesion?.perfil?.puedeResolver == true

    LaunchedEffect(aviso) {
        if (aviso != null) {
            kotlinx.coroutines.delay(3000)
            vm.limpiarAviso()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {

        AnimatedVisibility(visible = aviso != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    text = aviso.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        if (!esDireccion) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Puedes consultar el ranking. Seleccionar o descartar es exclusivo de Dirección.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val filas = estado.conValoraciones
        if (filas.isEmpty()) {
            EstadoVacio(
                icono = Icons.Filled.Insights,
                titulo = if (estado.cargando) "Calculando resultados…" else "Sin resultados todavía",
                detalle = "El ranking aparece cuando las postulantes empiezan a recibir valoraciones."
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = filas, key = { it.postulanteId }) { fila ->
                    FilaResultado(
                        posicion = filas.indexOf(fila) + 1,
                        fila = fila,
                        esDireccion = esDireccion,
                        alAbrir = { alAbrirFicha(fila.postulanteId) },
                        alResolver = { decision -> vm.resolver(fila.postulanteId, decision) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

/** Una postulante dentro del ranking consolidado. */
@Composable
private fun FilaResultado(
    posicion: Int,
    fila: FilaConsolidada,
    esDireccion: Boolean,
    alAbrir: () -> Unit,
    alResolver: (EtapaPostulacion) -> Unit,
    modifier: Modifier = Modifier
) {
    val nivel = NivelDesempenio.desde(fila.promedio)

    Card(onClick = alAbrir, modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$posicion",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fila.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = fila.especialidad,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${fila.cantidadValoraciones} valoraciones · ${nivel.rotulo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formateador.puntaje(fila.promedio),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    EtiquetaEtapa(fila.etapa)
                }
            }

            if (esDireccion && fila.etapa.admiteResolucion) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { alResolver(EtapaPostulacion.DESCARTADA) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ThumbDown, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Descartar")
                    }
                    Button(
                        onClick = { alResolver(EtapaPostulacion.SELECCIONADA) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Seleccionar")
                    }
                }
            }
        }
    }
}
