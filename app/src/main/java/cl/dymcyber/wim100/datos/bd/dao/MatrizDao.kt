package cl.dymcyber.wim100.datos.bd.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.dymcyber.wim100.datos.bd.entidades.CriterioWim
import kotlinx.coroutines.flow.Flow

/** Consultas sobre la matriz de evaluación. */
@Dao
interface MatrizDao {

    @Insert
    suspend fun insertarTodos(criterios: List<CriterioWim>)

    @Query("SELECT * FROM criterios ORDER BY orden ASC")
    fun observarMatriz(): Flow<List<CriterioWim>>

    @Query("SELECT * FROM criterios ORDER BY orden ASC")
    suspend fun leerMatriz(): List<CriterioWim>

    @Query("SELECT COUNT(*) FROM criterios")
    suspend fun contar(): Int
}
