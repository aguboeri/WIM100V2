package cl.dymcyber.wim100.datos.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.dymcyber.wim100.datos.bd.entidades.DetalleValoracion
import cl.dymcyber.wim100.datos.bd.entidades.Valoracion
import kotlinx.coroutines.flow.Flow

/**
 * Consultas sobre las valoraciones y sus detalles por criterio.
 *
 * `contarDeEvaluadora` es la primera barrera contra duplicados: el repositorio
 * la ejecuta dentro de la transacción antes de intentar insertar.
 */
@Dao
interface ValoracionDao {

    @Insert
    suspend fun insertar(valoracion: Valoracion): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<DetalleValoracion>)

    @Query("SELECT COUNT(*) FROM valoraciones WHERE postulanteId = :postulanteId AND credencialEvaluadora = :credencial")
    suspend fun contarDeEvaluadora(postulanteId: Long, credencial: String): Int

    @Query("SELECT COUNT(*) FROM valoraciones WHERE postulanteId = :postulanteId")
    suspend fun contarDePostulante(postulanteId: Long): Int

    @Query("SELECT * FROM valoraciones WHERE postulanteId = :postulanteId ORDER BY registradaEn DESC")
    fun observarDePostulante(postulanteId: Long): Flow<List<Valoracion>>

    @Query("SELECT * FROM detalles_valoracion WHERE valoracionId = :valoracionId")
    suspend fun leerDetalles(valoracionId: Long): List<DetalleValoracion>

    @Query(
        """
        SELECT p.id AS postulanteId,
               p.nombre AS nombre,
               p.especialidad AS especialidad,
               p.etapa AS etapa,
               COUNT(v.id) AS cantidadValoraciones,
               IFNULL(AVG(v.puntajeTotal), 0.0) AS promedio
        FROM postulantes p
        LEFT JOIN valoraciones v ON v.postulanteId = p.id
        GROUP BY p.id
        ORDER BY promedio DESC, p.nombre ASC
        """
    )
    fun observarConsolidado(): Flow<List<FilaConsolidada>>
}

/**
 * Resultado de la consulta consolidada. No es una tabla: Room arma estos
 * objetos con las columnas que pide el SELECT, por eso cada alias calza con una
 * propiedad.
 */
data class FilaConsolidada(
    val postulanteId: Long,
    val nombre: String,
    val especialidad: String,
    val etapa: cl.dymcyber.wim100.core.dominio.EtapaPostulacion,
    val cantidadValoraciones: Int,
    val promedio: Double
)
