package cl.dymcyber.wim100.datos.bd.entidades

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion

/** Una postulación al programa WIM 100. */
@Entity(
    tableName = "postulantes",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Postulante(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val telefono: String,
    val especialidad: String,
    val aniosTrayectoria: Int,
    val sintesis: String,
    val perfilWeb: String,
    val cvUri: String? = null,
    val cvNombre: String? = null,
    val etapa: EtapaPostulacion = EtapaPostulacion.RECIBIDA,
    val registradaEn: Long = System.currentTimeMillis(),
    val registradaPor: String
) {
    val tieneCurriculum: Boolean get() = !cvUri.isNullOrBlank()

    companion object {
        /** Áreas de la convocatoria. */
        val ESPECIALIDADES = listOf(
            "Seguridad ofensiva",
            "Respuesta a incidentes",
            "Gobernanza y riesgo",
            "Seguridad en la nube",
            "Análisis forense",
            "Concientización y formación"
        )
    }
}
