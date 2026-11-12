package cl.dymcyber.wim100.datos.bd.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Un hecho registrado en la bitácora: quién hizo qué, sobre quién y cuándo. */
@Entity(
    tableName = "bitacora",
    indices = [Index(value = ["ocurridoEn"])]
)
data class EventoBitacora(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accion: String,
    val detalle: String,
    val autor: String,
    val perfil: String,
    val postulanteId: Long? = null,
    val ocurridoEn: Long = System.currentTimeMillis()
) {
    companion object {
        const val INGRESO = "Ingreso al sistema"
        const val REGISTRO = "Registro de postulación"
        const val VALORACION = "Valoración registrada"
        const val DUPLICADO = "Intento de valoración duplicada"
        const val RESOLUCION = "Resolución de Dirección"
        const val CV_ADJUNTO = "CV adjuntado"
    }
}
