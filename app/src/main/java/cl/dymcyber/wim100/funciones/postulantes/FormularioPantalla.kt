package cl.dymcyber.wim100.funciones.postulantes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.dymcyber.wim100.core.reglas.ReglasPostulante
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.disenio.componentes.CampoConReparo
import cl.dymcyber.wim100.disenio.componentes.TituloSeccion
import cl.dymcyber.wim100.disenio.componentes.sacudir
import cl.dymcyber.wim100.dispositivo.LectorDocumentos
import cl.dymcyber.wim100.funciones.FabricaVm

/**
 * Formulario de registro de una postulación.
 *
 * Cada campo se valida mientras se escribe; el botón Guardar revisa el
 * formulario completo y, si falta algo, se sacude en vez de guardar a medias.
 *
 * El CV se elige con el selector de documentos del sistema, que no requiere
 * permiso de almacenamiento: la app solo recibe el archivo que la usuaria marcó.
 */
@Composable
fun FormularioPantalla(
    alTerminar: () -> Unit,
    modifier: Modifier = Modifier,
    vm: FormularioVm = viewModel(factory = FabricaVm.Reglas)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var menuAbierto by remember { mutableStateOf(false) }

    val selectorDeCv = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        LectorDocumentos.conservarAcceso(contexto, uri)
        val documento = LectorDocumentos.describir(contexto, uri)
        vm.onCurriculum(documento?.uri, documento?.nombre, documento?.pesoBytes)
    }

    // Cuando el guardado termina bien, volvemos a la lista.
    LaunchedEffect(estado.guardado) {
        if (estado.guardado) alTerminar()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        TituloSeccion(
            texto = "Datos de la postulante",
            apoyo = "Solo datos ficticios: correos de prueba y teléfonos inventados."
        )

        CampoConReparo(
            campo = estado.nombre,
            etiqueta = "Nombre completo",
            alCambiar = vm::onNombre,
            marcadorDePosicion = "Nombre y apellido"
        )

        CampoConReparo(
            campo = estado.correo,
            etiqueta = "Correo de contacto",
            alCambiar = vm::onCorreo,
            tipoDeTeclado = KeyboardType.Email,
            apoyo = "Dominios permitidos: ${ReglasPostulante.DOMINIOS_DE_PRUEBA.joinToString(", ")}"
        )

        CampoConReparo(
            campo = estado.telefono,
            etiqueta = "Teléfono",
            alCambiar = vm::onTelefono,
            tipoDeTeclado = KeyboardType.Phone,
            apoyo = "Formato: +56 9 1234 5678"
        )

        // Área de especialidad: lista cerrada, para que el dato quede uniforme.
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { menuAbierto = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(
                        text = estado.especialidad ?: "Área de especialidad",
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(
                    expanded = menuAbierto,
                    onDismissRequest = { menuAbierto = false }
                ) {
                    Postulante.ESPECIALIDADES.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area) },
                            onClick = {
                                vm.onEspecialidad(area)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(visible = estado.reparoEspecialidad != null) {
                Text(
                    text = estado.reparoEspecialidad.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 14.dp, top = 4.dp)
                )
            }
        }

        CampoConReparo(
            campo = estado.trayectoria,
            etiqueta = "Años de experiencia",
            alCambiar = vm::onTrayectoria,
            tipoDeTeclado = KeyboardType.Number
        )

        CampoConReparo(
            campo = estado.sintesis,
            etiqueta = "Síntesis de trayectoria",
            alCambiar = vm::onSintesis,
            lineasMaximas = 5,
            contador = estado.largoSintesis to ReglasPostulante.SINTESIS_LARGO_MAXIMO
        )

        CampoConReparo(
            campo = estado.perfilWeb,
            etiqueta = "Perfil profesional",
            alCambiar = vm::onPerfilWeb,
            tipoDeTeclado = KeyboardType.Uri,
            apoyo = "Enlace ficticio de referencia, no se consulta ninguna API"
        )

        TituloSeccion(texto = "Currículum", apoyo = "PDF de prueba de hasta 5 MB.")

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (estado.cvNombre != null) Icons.Filled.CheckCircle else Icons.Filled.AttachFile,
                    contentDescription = null,
                    tint = if (estado.cvNombre != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = estado.cvNombre ?: "Sin CV adjunto",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (estado.reparoCv != null) {
                        Text(
                            text = estado.reparoCv.orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (estado.cvNombre != null) {
                    IconButton(onClick = vm::quitarCurriculum) {
                        Icon(Icons.Filled.Delete, contentDescription = "Quitar CV")
                    }
                }
                OutlinedButton(onClick = { selectorDeCv.launch(LectorDocumentos.TIPOS_ACEPTADOS) }) {
                    Text(if (estado.cvNombre == null) "Adjuntar" else "Cambiar")
                }
            }
        }

        AnimatedVisibility(visible = estado.avisoGeneral != null, enter = fadeIn(), exit = fadeOut()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = estado.avisoGeneral.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
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
                Text("Registrar postulación")
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}
