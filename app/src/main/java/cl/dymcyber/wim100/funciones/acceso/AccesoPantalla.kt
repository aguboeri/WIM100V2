@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package cl.dymcyber.wim100.funciones.acceso

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.dymcyber.wim100.core.dominio.PerfilUsuario
import cl.dymcyber.wim100.disenio.componentes.CampoConReparo
import cl.dymcyber.wim100.dispositivo.VerificacionBiometrica
import cl.dymcyber.wim100.dispositivo.comoActividad
import cl.dymcyber.wim100.funciones.FabricaVm

/** Pantalla de ingreso. */
@Composable
fun AccesoPantalla(
    alEntrar: () -> Unit,
    vm: AccesoVm = viewModel(factory = FabricaVm.Reglas)
) {
    val estado by vm.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    val actividad = contexto.comoActividad()
    val estadoBiometria = VerificacionBiometrica.revisar(contexto)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "WIM 100",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Postulación y evaluación de candidatas · DYM Cyber",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(28.dp))

        Text("Ingresa con tu perfil", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            PerfilUsuario.entries.forEach { perfil ->
                val elegido = estado.perfil == perfil
                if (elegido) {
                    Button(onClick = { vm.onPerfil(perfil) }, modifier = Modifier.weight(1f)) {
                        Text(perfil.rotulo)
                    }
                } else {
                    OutlinedButton(onClick = { vm.onPerfil(perfil) }, modifier = Modifier.weight(1f)) {
                        Text(perfil.rotulo)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = estado.perfil.resumen,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        CampoConReparo(
            campo = estado.nombre,
            etiqueta = if (estado.perfil == PerfilUsuario.EVALUADORA) "Nombre de la evaluadora" else "Nombre (Dirección)",
            alCambiar = vm::onNombre,
            apoyo = "Tu nombre firma cada acción en la bitácora",
            marcadorDePosicion = "Nombre y apellido"
        )

        AnimatedVisibility(visible = estado.perfil.exigeVerificacion, enter = fadeIn(), exit = fadeOut()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = when (estadoBiometria) {
                            VerificacionBiometrica.Estado.DISPONIBLE ->
                                "Dirección resuelve postulaciones: se pedirá tu huella o rostro."
                            VerificacionBiometrica.Estado.SIN_REGISTRO ->
                                "Este equipo no tiene huella registrada. Se permite el ingreso y queda anotado en la bitácora."
                            VerificacionBiometrica.Estado.SIN_SENSOR ->
                                "Este equipo no tiene sensor biométrico. Se permite el ingreso y queda anotado en la bitácora."
                            VerificacionBiometrica.Estado.NO_DISPONIBLE ->
                                "La verificación no está disponible ahora. El ingreso queda anotado en la bitácora."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        AnimatedVisibility(visible = estado.aviso != null, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = estado.aviso.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (!vm.formularioValido()) return@Button
                // Se copia a una variable local para que Kotlin pueda
                // asegurar que no es nula dentro del bloque.
                val estaActividad = actividad
                val necesitaHuella = estado.perfil.exigeVerificacion &&
                    estadoBiometria == VerificacionBiometrica.Estado.DISPONIBLE

                if (necesitaHuella && estaActividad != null) {
                    vm.marcandoVerificacion(true)
                    VerificacionBiometrica.pedir(
                        actividad = estaActividad,
                        titulo = "Ingreso como Dirección",
                        subtitulo = "Verifica tu identidad para resolver postulaciones",
                        alAprobar = { vm.entrar(alEntrar) },
                        alRechazar = { motivo -> vm.avisar(motivo) }
                    )
                } else {
                    vm.entrar(alEntrar)
                }
            },
            enabled = !estado.verificando,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(if (estado.verificando) "Verificando…" else "Entrar")
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = "MVP académico. Todos los datos son ficticios.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
