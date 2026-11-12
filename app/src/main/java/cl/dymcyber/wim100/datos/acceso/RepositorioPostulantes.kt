package cl.dymcyber.wim100.datos.acceso

import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.datos.bd.dao.BitacoraDao
import cl.dymcyber.wim100.datos.bd.dao.MatrizDao
import cl.dymcyber.wim100.datos.bd.dao.PostulanteDao
import cl.dymcyber.wim100.datos.bd.entidades.CriterioWim
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import kotlinx.coroutines.flow.Flow

/** Se lanza cuando el correo de una postulante ya está registrado. */
class CorreoRepetidoException(mensaje: String) : Exception(mensaje)

/** Puerta de entrada a los datos de postulantes. */
class RepositorioPostulantes(
    private val postulanteDao: PostulanteDao,
    private val criterioDao: MatrizDao,
    private val bitacoraDao: BitacoraDao
) {

    fun observarTodas(): Flow<List<Postulante>> = postulanteDao.observarTodas()

    fun observarUna(id: Long): Flow<Postulante?> = postulanteDao.observarUna(id)

    fun observarMatriz(): Flow<List<CriterioWim>> = criterioDao.observarMatriz()

    suspend fun buscarPorId(id: Long): Postulante? = postulanteDao.buscarPorId(id)

    /**
     * Registra una postulación y deja constancia en la bitácora.
     *
     * @throws CorreoRepetidoException si el correo ya existe. El índice único de
     */
    suspend fun registrar(postulante: Postulante, sesion: Sesion): Long {
        if (postulanteDao.contarConCorreo(postulante.correo) > 0) {
            throw CorreoRepetidoException("Ya existe una postulación con ese correo")
        }
        val id = postulanteDao.insertar(postulante.copy(registradaPor = sesion.nombre))
        anotar(
            accion = EventoBitacora.REGISTRO,
            detalle = "Se registró a ${postulante.nombre}",
            sesion = sesion,
            postulanteId = id
        )
        return id
    }

    /** Guarda el CV elegido con el selector de documentos. */
    suspend fun adjuntarCurriculum(id: Long, uri: String?, nombreArchivo: String?, sesion: Sesion) {
        postulanteDao.adjuntarCurriculum(id, uri, nombreArchivo)
        anotar(
            accion = EventoBitacora.CV_ADJUNTO,
            detalle = nombreArchivo?.let { "Se adjuntó $it" } ?: "Se quitó el CV",
            sesion = sesion,
            postulanteId = id
        )
    }

    /** Cierra una postulación. */
    suspend fun resolver(id: Long, decision: EtapaPostulacion, sesion: Sesion) {
        require(decision.estaCerrada) { "La resolución debe ser Seleccionada o Descartada" }
        require(sesion.perfil.puedeResolver) { "Solo Dirección puede resolver una postulación" }

        val postulante = postulanteDao.buscarPorId(id) ?: return
        if (!postulante.etapa.admiteResolucion) return

        postulanteDao.cambiarEtapa(id, decision)
        anotar(
            accion = EventoBitacora.RESOLUCION,
            detalle = "${postulante.nombre}: ${decision.rotulo}",
            sesion = sesion,
            postulanteId = id
        )
    }

    private suspend fun anotar(accion: String, detalle: String, sesion: Sesion, postulanteId: Long?) {
        bitacoraDao.registrar(
            EventoBitacora(
                accion = accion,
                detalle = detalle,
                autor = sesion.nombre,
                perfil = sesion.perfil.rotulo,
                postulanteId = postulanteId
            )
        )
    }
}
