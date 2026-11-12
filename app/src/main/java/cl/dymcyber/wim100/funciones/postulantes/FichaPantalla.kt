package cl.dymcyber.wim100.funciones.postulantes

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.formato.Formateador
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.datos.bd.entidades.Valoracion
import cl.dymcyber.wim100.disenio.componentes.EtiquetaEtapa
import cl.dymcyber.wim100.disenio.componentes.FilaDato
import cl.dymcyber.wim100.disenio.componentes.MedidorPuntaje
import cl.dymcyber.wim100.disenio.componentes.TituloSeccion
import cl.dymcyber.wim100.dispositivo.LectorDocumentos
import cl.dymcyber.wim100.dispositivo.PuenteContacto
import cl.dymcyber.wim100.funciones.FabricaVm

/**
 * Ficha completa de una postulante.
 *
 * Reúne tres cosas: sus antecedentes, las valoraciones recibidas y las acciones
 * que su etapa y el perfil de quien mira permiten. Dos recursos nativos se usan
 * aquí: el visor del CV en PDF y los intents de contacto.
 */
@Composable
fun FichaPantalla(
    alValorar: (Long) -> Unit,
    modifier: Modifier = Modifier,
    vm: FichaVm = viewModel(factory = FabricaVm.Reglas)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val sesion by vm.sesion.collectAsStateWithLifecycle()
    val detalles by vm.detalles.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val nombresCriterios by vm.nombresCriterios.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val postulante = estado.postulante

    val selectorDeCv = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        LectorDocumentos.conservarAcceso(contexto, uri)
        val documento = LectorDocumentos.describir(contexto, uri)
        vm.adjuntarCurriculum(documento?.uri, documento?.nombre)
    }

    if (postulante == null) {
        Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
            Text(
                text = if (estado.cargando) "Cargando ficha…" else "La postulación ya no existe.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // Encabezado con el promedio consolidado
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = postulante.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = postulante.especialidad,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    EtiquetaEtapa(postulante.etapa)
                }
                if (estado.cantidadValoraciones > 0) {
                    MedidorPuntaje(puntaje = estado.promedio, diametro = 84)
                }
            }
        }

        // Contacto mediante intents: la app no llama ni envía nada por su cuenta
        TituloSeccion(
            texto = "Contacto",
            apoyo = "Se abre la app correspondiente del teléfono; tú decides si continuar."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AssistChip(
                onClick = { PuenteContacto.marcar(contexto, postulante.telefono) },
                label = { Text("Llamar") },
                leadingIcon = { Icon(Icons.Filled.Call, contentDescription = null, Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = {
                    PuenteContacto.escribirCorreo(
                        contexto,
                        postulante.correo,
                        "Programa WIM 100"
                    )
                },
                label = { Text("Correo") },
                leadingIcon = { Icon(Icons.Filled.Mail, contentDescription = null, Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = { PuenteContacto.abrirPerfil(contexto, postulante.perfilWeb) },
                label = { Text("Perfil") },
                leadingIcon = { Icon(Icons.Filled.Language, contentDescription = null, Modifier.size(18.dp)) }
            )
        }

        // Antecedentes
        TituloSeccion(texto = "Antecedentes")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                FilaDato("Experiencia", Formateador.anios(postulante.aniosTrayectoria))
                FilaDato("Correo", postulante.correo)
                FilaDato("Teléfono", postulante.telefono)
                FilaDato("Registrada por", postulante.registradaPor)
                FilaDato("Fecha", Formateador.fecha(postulante.registradaEn))
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                Text(
                    text = postulante.sintesis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Currículum: recurso nativo (selector de documentos + PdfRenderer)
        TituloSeccion(texto = "Currículum", apoyo = "PDF de prueba, visto dentro de la app.")
        TarjetaCurriculum(
            postulante = postulante,
            alAdjuntar = { selectorDeCv.launch(LectorDocumentos.TIPOS_ACEPTADOS) },
            alAbrirAfuera = { PuenteContacto.abrirDocumento(contexto, it) }
        )

        // Acciones según perfil y etapa
        TituloSeccion(texto = "Acciones")
        AccionesSegunPerfil(
            etapa = postulante.etapa,
            esDireccion = sesion?.perfil?.puedeResolver == true,
            yaValoro = estado.yaValoro(sesion?.credencial.orEmpty()),
            cantidadValoraciones = estado.cantidadValoraciones,
            alValorar = { alValorar(postulante.id) },
            alResolver = vm::resolver
        )

        AnimatedVisibility(visible = aviso != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = aviso.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // Valoraciones recibidas
        TituloSeccion(
            texto = "Valoraciones recibidas",
            apoyo = "${estado.cantidadValoraciones} de ${EtapaPostulacion.VALORACIONES_PARA_CERRAR_REVISION} necesarias para resolver."
        )
        if (estado.valoraciones.isEmpty()) {
            Text(
                text = "Todavía nadie la ha valorado.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            estado.valoraciones.forEach { valoracion ->
                TarjetaValoracion(
                    valoracion = valoracion,
                    detalleCargado = detalles[valoracion.id] != null,
                    alDesplegar = { vm.cargarDetalle(valoracion.id) },
                    contenidoDetalle = {
                        detalles[valoracion.id]?.forEach { fila ->
                            FilaDato(
                                etiqueta = "${nombresCriterios[fila.criterioId] ?: "Criterio"} · peso ${fila.pesoAplicado}",
                                valor = "${fila.puntaje}/5"
                            )
                            if (fila.observacion.isNotBlank()) {
                                Text(
                                    text = fila.observacion,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }

    LaunchedEffect(aviso) {
        if (aviso != null) {
            kotlinx.coroutines.delay(3500)
            vm.limpiarAviso()
        }
    }
}

/** Tarjeta del CV: adjuntar, previsualizar la primera página y abrir fuera. */
@Composable
private fun TarjetaCurriculum(
    postulante: Postulante,
    alAdjuntar: () -> Unit,
    alAbrirAfuera: (String) -> Unit
) {
    val contexto = LocalContext.current
    var mostrandoVista by remember { mutableStateOf(false) }
    val rutaCv = postulante.cvUri

    val vistaPrevia = remember(rutaCv, mostrandoVista) {
        if (!mostrandoVista || rutaCv == null) null
        else LectorDocumentos.primeraPagina(contexto, rutaCv.toUri())
    }

    Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (postulante.tieneCurriculum) Icons.Filled.CheckCircle else Icons.Filled.AttachFile,
                    contentDescription = null,
                    tint = if (postulante.tieneCurriculum) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = postulante.cvNombre ?: "Sin CV adjunto",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(onClick = alAdjuntar) {
                    Text(if (postulante.tieneCurriculum) "Cambiar" else "Adjuntar")
                }
            }

            if (postulante.tieneCurriculum) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { mostrandoVista = !mostrandoVista }) {
                        Text(if (mostrandoVista) "Ocultar vista previa" else "Ver primera página")
                    }
                    OutlinedButton(onClick = { rutaCv?.let(alAbrirAfuera) }) {
                        Text("Abrir en otra app")
                    }
                }

                AnimatedVisibility(visible = mostrandoVista) {
                    if (vistaPrevia != null) {
                        Image(
                            bitmap = vistaPrevia.asImageBitmap(),
                            contentDescription = "Primera página del CV",
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                        )
                    } else {
                        Text(
                            text = "No se pudo leer el PDF. Puede que el archivo ya no exista.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Qué puede hacer quien mira la ficha.
 *
 * Es una estructura de control sobre dos datos: el perfil de la sesión y la
 * etapa de la postulación. Las reglas del caso quedan visibles aquí: solo
 * Dirección resuelve, solo con dos valoraciones, y nunca sobre un caso cerrado.
 */
@Composable
private fun AccionesSegunPerfil(
    etapa: EtapaPostulacion,
    esDireccion: Boolean,
    yaValoro: Boolean,
    cantidadValoraciones: Int,
    alValorar: () -> Unit,
    alResolver: (EtapaPostulacion) -> Unit
) {
    when {
        etapa.estaCerrada -> AvisoBloqueo(
            texto = "Postulación resuelta: ${etapa.rotulo}. No admite nuevas valoraciones."
        )

        esDireccion && etapa.admiteResolucion -> Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { alResolver(EtapaPostulacion.DESCARTADA) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ThumbDown, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Descartar")
            }
            Button(
                onClick = { alResolver(EtapaPostulacion.SELECCIONADA) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.ThumbUp, contentDescription = null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Seleccionar")
            }
        }

        esDireccion -> AvisoBloqueo(
            texto = "Para resolver se necesitan ${EtapaPostulacion.VALORACIONES_PARA_CERRAR_REVISION} valoraciones " +
                "($cantidadValoraciones registradas)."
        )

        yaValoro -> AvisoBloqueo(
            texto = "Ya registraste tu valoración. El sistema impide una segunda.",
            icono = true
        )

        else -> Button(
            onClick = alValorar,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Valorar con la matriz WIM")
        }
    }
}

@Composable
private fun AvisoBloqueo(texto: String, icono: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (icono) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = texto,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Tarjeta de una valoración, que se despliega para ver el detalle por criterio. */
@Composable
private fun TarjetaValoracion(
    valoracion: Valoracion,
    detalleCargado: Boolean,
    alDesplegar: () -> Unit,
    contenidoDetalle: @Composable () -> Unit
) {
    var abierta by remember { mutableStateOf(false) }
    val giro by animateFloatAsState(if (abierta) 180f else 0f, label = "giro del ícono")

    Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(valoracion.evaluadora, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = Formateador.fechaYHora(valoracion.registradaEn),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = Formateador.puntaje(valoracion.puntajeTotal),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                OutlinedButton(
                    onClick = {
                        abierta = !abierta
                        if (!detalleCargado) alDesplegar()
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = if (abierta) "Ocultar detalle" else "Ver detalle",
                        modifier = Modifier.rotate(giro)
                    )
                }
            }

            AnimatedVisibility(visible = abierta) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    contenidoDetalle()
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = valoracion.cierre,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
