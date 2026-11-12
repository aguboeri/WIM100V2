package cl.dymcyber.wim100.dispositivo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Recurso nativo: intents implícitos hacia otras apps del teléfono. */
object PuenteContacto {

    /** Abre el marcador con el número cargado, sin llamar automáticamente. */
    fun marcar(contexto: Context, telefono: String): Boolean =
        lanzar(contexto, Intent(Intent.ACTION_DIAL, Uri.parse("tel:${telefono.filter { it.isDigit() || it == '+' }}")))

    /** Abre la app de correo con el destinatario y el asunto ya escritos. */
    fun escribirCorreo(contexto: Context, correo: String, asunto: String): Boolean {
        val intencion = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correo")).apply {
            putExtra(Intent.EXTRA_SUBJECT, asunto)
        }
        return lanzar(contexto, intencion)
    }

    /** Abre el perfil profesional en el navegador. */
    fun abrirPerfil(contexto: Context, url: String): Boolean =
        lanzar(contexto, Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    /** Abre el CV con el visor de PDF que la usuaria tenga instalado. */
    fun abrirDocumento(contexto: Context, uri: String): Boolean {
        val intencion = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(uri), "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return lanzar(contexto, intencion)
    }

    /** Comparte un resumen de texto con cualquier app del sistema. */
    fun compartir(contexto: Context, titulo: String, texto: String): Boolean {
        val intencion = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        return lanzar(contexto, Intent.createChooser(intencion, titulo))
    }

    private fun lanzar(contexto: Context, intencion: Intent): Boolean = try {
        contexto.startActivity(intencion)
        true
    } catch (sinApp: ActivityNotFoundException) {
        false
    }
}
