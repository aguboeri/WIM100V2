package cl.dymcyber.wim100.datos.bd.entidades

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Un criterio de la matriz de evaluación WIM.
 *
 * El peso indica cuánto influye en el puntaje final. Los pesos de la matriz
 * completa deben sumar 100; esa regla se revisa en `ReglasValoracion`.
 *
 * La matriz definitiva queda sujeta a validación del Socio Formador, por eso
 * vive en la base de datos y no escrita a mano en el código.
 */
@Entity(tableName = "criterios")
data class CriterioWim(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String,
    val peso: Int,
    val orden: Int
)
