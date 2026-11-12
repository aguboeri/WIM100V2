package cl.dymcyber.wim100.datos.bd.entidades

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** El puntaje y la observación de un criterio dentro de una valoración. */
@Entity(
    tableName = "detalles_valoracion",
    foreignKeys = [
        ForeignKey(
            entity = Valoracion::class,
            parentColumns = ["id"],
            childColumns = ["valoracionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CriterioWim::class,
            parentColumns = ["id"],
            childColumns = ["criterioId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["valoracionId"]), Index(value = ["criterioId"])]
)
data class DetalleValoracion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val valoracionId: Long,
    val criterioId: Long,
    val puntaje: Int,
    val observacion: String,
    val pesoAplicado: Int
)
