package cl.dymcyber.wim100.funciones.cuenta

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
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.disenio.componentes.TituloSeccion
import cl.dymcyber.wim100.dispositivo.AvisosLocales
import cl.dymcyber.wim100.dispositivo.VerificacionBiometrica

/** Cuenta: sesión activa y estado de los recursos del dispositivo. */
@Composable
fun CuentaPantalla(
    sesion: Sesion?,
    alCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val biometria = VerificacionBiometrica.revisar(contexto)
    val avisosPermitidos = AvisosLocales.permitido(contexto)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = sesion?.nombre ?: "Sin sesión",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sesion?.perfil?.rotulo.orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = sesion?.perfil?.resumen.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        TituloSeccion(
            texto = "Recursos del dispositivo",
            apoyo = "Cuatro recursos nativos integrados en el flujo."
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                FilaRecurso(
                    icono = Icons.Filled.AttachFile,
                    nombre = "Selector de documentos",
                    permiso = "No requiere permiso",
                    detalle = "La app solo recibe el PDF que tú eliges.",
                    activo = true
                )
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                FilaRecurso(
                    icono = Icons.Filled.Link,
                    nombre = "Intents de contacto",
                    permiso = "No requiere permiso",
                    detalle = "Abre el marcador, el correo o el navegador; tú confirmas.",
                    activo = true
                )
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                FilaRecurso(
                    icono = Icons.Filled.Notifications,
                    nombre = "Notificaciones locales",
                    permiso = "POST_NOTIFICATIONS (Android 13+)",
                    detalle = if (avisosPermitidos) "Concedido: avisará al registrar una valoración."
                    else "Sin permiso: la app funciona igual, pero no avisa.",
                    activo = avisosPermitidos
                )
                HorizontalDivider(Modifier.padding(vertical = 10.dp))
                FilaRecurso(
                    icono = Icons.Filled.Fingerprint,
                    nombre = "Verificación biométrica",
                    permiso = "USE_BIOMETRIC (permiso normal)",
                    detalle = when (biometria) {
                        VerificacionBiometrica.Estado.DISPONIBLE -> "Disponible: se exige para entrar como Dirección."
                        VerificacionBiometrica.Estado.SIN_REGISTRO -> "Sin huella registrada en este equipo."
                        VerificacionBiometrica.Estado.SIN_SENSOR -> "Este equipo no tiene sensor."
                        VerificacionBiometrica.Estado.NO_DISPONIBLE -> "No disponible en este momento."
                    },
                    activo = biometria == VerificacionBiometrica.Estado.DISPONIBLE
                )
            }
        }

        TituloSeccion(texto = "Sobre la aplicación")
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "WIM 100 · MVP académico para DYM Cyber",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Kotlin con Jetpack Compose y Material 3, arquitectura MVVM y persistencia local con Room sobre SQLite. Todos los datos son ficticios y la app no se conecta a ningún servicio externo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        OutlinedButton(
            onClick = alCerrarSesion,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilaRecurso(
    icono: ImageVector,
    nombre: String,
    permiso: String,
    detalle: String,
    activo: Boolean
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                text = permiso,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = detalle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = if (activo) Icons.Filled.CheckCircle else Icons.Filled.RemoveCircleOutline,
            contentDescription = if (activo) "Disponible" else "No disponible",
            tint = if (activo) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}
