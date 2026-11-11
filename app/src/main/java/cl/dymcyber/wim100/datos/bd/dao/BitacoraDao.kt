package cl.dymcyber.wim100.datos.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import kotlinx.coroutines.flow.Flow

/** Consultas sobre la bitácora de acciones. */
@Dao
interface BitacoraDao {

    @Insert
    suspend fun registrar(evento: EventoBitacora)

    @Query("SELECT * FROM bitacora ORDER BY ocurridoEn DESC LIMIT :tope")
    fun observarRecientes(tope: Int): Flow<List<EventoBitacora>>

    @Query("SELECT * FROM bitacora WHERE postulanteId = :postulanteId ORDER BY ocurridoEn DESC")
    fun observarDePostulante(postulanteId: Long): Flow<List<EventoBitacora>>
}
