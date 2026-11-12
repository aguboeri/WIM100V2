package cl.dymcyber.wim100.datos.bd.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Un criterio de la matriz de evaluación WIM. */
@Entity(tableName = "criterios")
data class CriterioWim(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val peso: Int,
    val orden: Int
)
