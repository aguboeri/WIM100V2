package cl.dymcyber.wim100.disenio.componentes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.dominio.NivelDesempenio
import cl.dymcyber.wim100.core.formato.Formateador
import cl.dymcyber.wim100.disenio.AmbarAtencion
import cl.dymcyber.wim100.disenio.AmbarSuave
import cl.dymcyber.wim100.disenio.RojoFondo
import cl.dymcyber.wim100.disenio.RojoReparo
import cl.dymcyber.wim100.disenio.VerdeContenedor
import cl.dymcyber.wim100.disenio.VerdeProfundo

/** Etiqueta de color con la etapa de una postulación. */
@Composable
fun EtiquetaEtapa(etapa: EtapaPostulacion, modifier: Modifier = Modifier) {
    val fondo: Color
    val tinta: Color
    when (etapa) {
        EtapaPostulacion.SELECCIONADA -> { fondo = VerdeContenedor; tinta = VerdeProfundo }
        EtapaPostulacion.DESCARTADA -> { fondo = RojoFondo; tinta = RojoReparo }
        EtapaPostulacion.EVALUADA -> { fondo = AmbarSuave; tinta = AmbarAtencion }
        else -> {
            fondo = MaterialTheme.colorScheme.surfaceVariant
            tinta = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Surface(
        color = fondo,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Text(
            text = etapa.rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = tinta,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/** Medidor circular del puntaje. */
@Composable
fun MedidorPuntaje(
    puntaje: Double,
    modifier: Modifier = Modifier,
    diametro: Int = 92
) {
    val objetivo = (puntaje / 100.0).toFloat().coerceIn(0f, 1f)
    val avance by animateFloatAsState(
        targetValue = objetivo,
        animationSpec = tween(durationMillis = 600),
        label = "avance del puntaje"
    )
    val nivel = NivelDesempenio.desde(puntaje)
    val tinta by animateColorAsState(
        targetValue = when (nivel) {
            NivelDesempenio.SOBRESALIENTE -> MaterialTheme.colorScheme.primary
            NivelDesempenio.SOLIDA -> MaterialTheme.colorScheme.secondary
            NivelDesempenio.EN_DESARROLLO -> AmbarAtencion
            NivelDesempenio.INSUFICIENTE -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(durationMillis = 600),
        label = "color del puntaje"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(diametro.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 7.dp
            )
            CircularProgressIndicator(
                progress = { avance },
                modifier = Modifier.size(diametro.dp),
                color = tinta,
                strokeWidth = 7.dp
            )
            Text(
                text = Formateador.puntaje(puntaje),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = nivel.rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

/** Título de sección, para separar bloques dentro de una pantalla. */
@Composable
fun TituloSeccion(texto: String, apoyo: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = texto,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (apoyo != null) {
            Text(
                text = apoyo,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Fila con una etiqueta a la izquierda y su valor a la derecha. */
@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
