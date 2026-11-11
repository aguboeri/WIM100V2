package cl.dymcyber.wim100

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import cl.dymcyber.wim100.disenio.TemaWim

/**
 * Única Activity de la app: todo lo demás son Composables.
 *
 * Hereda de FragmentActivity y no de ComponentActivity porque BiometricPrompt,
 * el diálogo del sistema que verifica la huella, lo exige.
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TemaWim {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppWim()
                }
            }
        }
    }
}
