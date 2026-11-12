package cl.dymcyber.wim100.dispositivo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns

/** Lo que devuelve el selector del sistema cuando se elige un documento. */
data class DocumentoElegido(
    val uri: String,
    val nombre: String,
    val pesoBytes: Long
)

/** Recurso nativo: selector de documentos del sistema y visor de PDF. */
object LectorDocumentos {

    val TIPOS_ACEPTADOS = arrayOf("application/pdf")

    /** Pide permiso permanente de lectura sobre el archivo elegido. */
    fun conservarAcceso(contexto: Context, uri: Uri) {
        runCatching {
            contexto.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    /** Lee el nombre y el peso del archivo desde el proveedor de documentos. */
    fun describir(contexto: Context, uri: Uri): DocumentoElegido? {
        val cursor = contexto.contentResolver.query(uri, null, null, null, null) ?: return null
        cursor.use {
            if (!it.moveToFirst()) return null
            val columnaNombre = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val columnaPeso = it.getColumnIndex(OpenableColumns.SIZE)
            val nombre = if (columnaNombre >= 0) it.getString(columnaNombre) else "documento.pdf"
            val peso = if (columnaPeso >= 0 && !it.isNull(columnaPeso)) it.getLong(columnaPeso) else 0L
            return DocumentoElegido(uri = uri.toString(), nombre = nombre.orEmpty(), pesoBytes = peso)
        }
    }

    /**
     * Dibuja la primera página del PDF como imagen, para mostrarla dentro de la app sin depender de otra aplicación.
     *
     * @return null si el archivo ya no existe o no se puede leer.
     */
    fun primeraPagina(contexto: Context, uri: Uri, anchoDeseado: Int = 1080): Bitmap? = runCatching {
        val descriptor: ParcelFileDescriptor =
            contexto.contentResolver.openFileDescriptor(uri, "r") ?: return null

        descriptor.use { archivo ->
            PdfRenderer(archivo).use { lector ->
                if (lector.pageCount == 0) return null
                lector.openPage(0).use { pagina ->
                    val escala = anchoDeseado.toFloat() / pagina.width
                    val alto = (pagina.height * escala).toInt().coerceAtLeast(1)
                    val lienzo = Bitmap.createBitmap(anchoDeseado, alto, Bitmap.Config.ARGB_8888)
                    android.graphics.Canvas(lienzo).drawColor(Color.WHITE)
                    pagina.render(lienzo, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    lienzo
                }
            }
        }
    }.getOrNull()
}
