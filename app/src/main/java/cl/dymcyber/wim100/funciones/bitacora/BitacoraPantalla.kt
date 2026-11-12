package cl.dymcyber.wim100.funciones.bitacora

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import cl.dymcyber.wim100.core.formato.Formateador
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import cl.dymcyber.wim100.disenio.componentes.EstadoVacio
import cl.dymcyber.wim100.funciones.FabricaVm

/** Bitácora de acciones: la trazabilidad del proceso. */
@Composable
fun BitacoraPantalla(
    modifier: Modifier = Modifier,
    vm: BitacoraVm = viewModel(factory = FabricaVm.Reglas)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {

        OutlinedTextField(
            value = busqueda,
            onValueChange = vm::onBuscar,
            label = { Text("Buscar en la bitácora") },
            placeholder = { Text("Acción, persona o detalle") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (busqueda.isNotEmpty()) {
                    IconButton(onClick = { vm.onBuscar("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Limpiar")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (estado.eventos.isEmpty()) {
            EstadoVacio(
                icono = Icons.Filled.History,
                titulo = if (estado.cargando) "Cargando bitácora…" else "Sin registros",
                detalle = "Aquí aparecerán los ingresos, registros, valoraciones y resoluciones."
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, bottom = 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = estado.eventos, key = { it.id }) { evento ->
                    TarjetaEvento(evento, Modifier.animateItem())
                }
            }
        }
    }
}

@Composable
private fun TarjetaEvento(evento: EventoBitacora, modifier: Modifier = Modifier) {
    val esIntentoDuplicado = evento.accion == EventoBitacora.DUPLICADO

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (esIntentoDuplicado) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = evento.accion,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = Formateador.fechaYHora(evento.ocurridoEn),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = evento.detalle, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${evento.autor} · ${evento.perfil}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
