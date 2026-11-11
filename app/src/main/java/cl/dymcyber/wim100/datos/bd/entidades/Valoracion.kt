package cl.dymcyber.wim100.datos.bd.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una evaluación completa hecha por una evaluadora a una postulante.
 *
 * El índice único sobre (postulanteId, credencialEvaluadora) es la barrera de
 * la base de datos contra evaluaciones duplicadas: aunque el código fallara, el
 * motor rechazaría la segunda fila. La credencial se calcula en `Sesion`, así
 * que escribir el nombre con otras mayúsculas no sirve para evaluar dos veces.
 */
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
