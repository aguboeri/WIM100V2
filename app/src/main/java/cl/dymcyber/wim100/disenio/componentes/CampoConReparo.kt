package cl.dymcyber.wim100.disenio.componentes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.dymcyber.wim100.funciones.Campo

/**
 * Campo de texto con su mensaje de error.
 *
 * El componente no sabe nada de reglas de negocio: recibe el [campo] ya
 * revisado por el ViewModel y se limita a mostrarlo. Si hay reparo, pinta el
 * borde de error, muestra el ícono y despliega el mensaje con una animación
 * corta para que el cambio se note sin que la pantalla salte.
 */
@Composable
fun CampoConReparo(
    campo: Campo,
    etiqueta: String,
    alCambiar: (String) -> Unit,
    modifier: Modifier = Modifier,
    apoyo: String? = null,
    marcadorDePosicion: String? = null,
    tipoDeTeclado: KeyboardType = KeyboardType.Text,
    lineasMaximas: Int = 1,
    contador: Pair<Int, Int>? = null,
    habilitado: Boolean = true
) {
    val hayReparo = campo.reparo != null

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = campo.valor,
            onValueChange = alCambiar,
            label = { Text(etiqueta) },
            placeholder = marcadorDePosicion?.let { { Text(it) } },
            isError = hayReparo,
            enabled = habilitado,
            singleLine = lineasMaximas == 1,
            maxLines = lineasMaximas,
            keyboardOptions = KeyboardOptions(keyboardType = tipoDeTeclado),
            trailingIcon = if (hayReparo) {
                {
                    Icon(
                        imageVector = Icons.Filled.ErrorOutline,
                        contentDescription = "Campo con error",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(
            visible = hayReparo,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 14.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = campo.reparo.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (!hayReparo && (apoyo != null || contador != null)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 4.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = apoyo.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (contador != null) {
                    Text(
                        text = "${contador.first}/${contador.second}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
