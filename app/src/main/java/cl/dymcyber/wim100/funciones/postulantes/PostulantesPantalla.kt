@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.dymcyber.wim100.funciones.postulantes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.formato.Formateador
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.disenio.componentes.EstadoVacio
import cl.dymcyber.wim100.disenio.componentes.EtiquetaEtapa
import cl.dymcyber.wim100.funciones.FabricaVm

/** Listado de postulaciones con búsqueda y filtro por etapa. */
@Composable
fun PostulantesPantalla(
    alAbrirFicha: (Long) -> Unit,
    modifier: Modifier = Modifier,
    vm: ListaVm = viewModel(factory = FabricaVm.Reglas)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val etapaElegida by vm.etapa.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        OutlinedTextField(
            value = busqueda,
            onValueChange = vm::onBuscar,
            label = { Text("Buscar") },
            placeholder = { Text("Nombre, área o correo") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (busqueda.isNotEmpty()) {
                    IconButton(onClick = { vm.onBuscar("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            EtapaPostulacion.entries.forEach { etapa ->
                FilterChip(
                    selected = etapaElegida == etapa,
                    onClick = { vm.onEtapa(etapa) },
                    label = { Text(etapa.rotulo) }
                )
            }
        }

        Text(
            text = when {
                estado.cargando -> "Cargando postulaciones…"
                else -> "${estado.visibles.size} de ${estado.total} postulaciones"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        when {
            estado.sinDatos -> EstadoVacio(
                icono = Icons.Filled.Groups,
                titulo = "Todavía no hay postulaciones",
                detalle = "Usa el botón de abajo para registrar la primera con datos ficticios."
            )

            estado.vacioPorFiltro -> EstadoVacio(
                icono = Icons.Filled.SearchOff,
                titulo = "Sin coincidencias",
                detalle = "Ninguna postulación calza con la búsqueda o el filtro elegido.",
                rotuloAccion = "Quitar filtros",
                alAccionar = vm::limpiarFiltros
            )

            else -> LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = estado.visibles, key = { it.id }) { postulante ->
                    TarjetaPostulante(
                        postulante = postulante,
                        alTocar = { alAbrirFicha(postulante.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

/** Resumen de una postulante dentro de la lista. */
@Composable
private fun TarjetaPostulante(
    postulante: Postulante,
    alTocar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = alTocar,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = postulante.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaEtapa(postulante.etapa)
            }
            Text(
                text = postulante.especialidad,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${Formateador.anios(postulante.aniosTrayectoria)} de trayectoria · ${postulante.correo}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (postulante.tieneCurriculum) {
                Text(
                    text = "CV adjunto",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
