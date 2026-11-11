package cl.dymcyber.wim100.datos.acceso

import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.datos.bd.dao.BitacoraDao
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import kotlinx.coroutines.flow.Flow

/** Lectura y escritura de la bitácora de acciones. */
class RepositorioBitacora(private val bitacoraDao: BitacoraDao) {

    fun observarRecientes(): Flow<List<EventoBitacora>> = bitacoraDao.observarRecientes(TOPE)

    fun observarDePostulante(postulanteId: Long): Flow<List<EventoBitacora>> =
        bitacoraDao.observarDePostulante(postulanteId)

    suspend fun anotarIngreso(sesion: Sesion) {
        bitacoraDao.registrar(
            EventoBitacora(
                accion = EventoBitacora.INGRESO,
                detalle = "Ingresó como ${sesion.perfil.rotulo}",
                autor = sesion.nombre,
                perfil = sesion.perfil.rotulo
            )
        )
    }

    private companion object {
        const val TOPE = 200
    }
}
