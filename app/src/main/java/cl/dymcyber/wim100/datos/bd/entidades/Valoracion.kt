package cl.dymcyber.wim100.datos.bd.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Una evaluación completa hecha por una evaluadora a una postulante. */
@Entity(
    tableName = "valoraciones",
    foreignKeys = [
        ForeignKey(
            entity = Postulante::class,
            parentColumns = ["id"],
            childColumns = ["postulanteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["postulanteId", "credencialEvaluadora"], unique = true),
        Index(value = ["postulanteId"])
    ]
)
data class Valoracion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postulanteId: Long,
    val evaluadora: String,
    val credencialEvaluadora: String,
    val puntajeTotal: Double,
    val cierre: String,
    val registradaEn: Long = System.currentTimeMillis()
)
