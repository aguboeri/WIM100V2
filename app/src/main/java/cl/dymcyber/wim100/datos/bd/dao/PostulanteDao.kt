package cl.dymcyber.wim100.datos.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import kotlinx.coroutines.flow.Flow

/**
 * Consultas sobre la tabla de postulantes.
 *
 * Las que devuelven `Flow` son reactivas: Room vuelve a emitir sola cada vez que
 * la tabla cambia, así la lista de la pantalla se actualiza sin recargar nada.
 */
@Dao
interface PostulanteDao {

    @Insert
    suspend fun insertar(postulante: Postulante): Long

    @Update
    suspend fun actualizar(postulante: Postulante)

    @Query("SELECT * FROM postulantes ORDER BY registradaEn DESC")
    fun observarTodas(): Flow<List<Postulante>>

    @Query("SELECT * FROM postulantes WHERE id = :id")
    fun observarUna(id: Long): Flow<Postulante?>

    @Query("SELECT * FROM postulantes WHERE id = :id")
    suspend fun buscarPorId(id: Long): Postulante?

    @Query("SELECT COUNT(*) FROM postulantes WHERE LOWER(TRIM(correo)) = LOWER(TRIM(:correo))")
    suspend fun contarConCorreo(correo: String): Int

    @Query("SELECT COUNT(*) FROM postulantes")
    suspend fun contarTodas(): Int

    @Query("UPDATE postulantes SET etapa = :etapa WHERE id = :id")
    suspend fun cambiarEtapa(id: Long, etapa: EtapaPostulacion)

    @Query("UPDATE postulantes SET cvUri = :uri, cvNombre = :nombre WHERE id = :id")
    suspend fun adjuntarCurriculum(id: Long, uri: String?, nombre: String?)
}
