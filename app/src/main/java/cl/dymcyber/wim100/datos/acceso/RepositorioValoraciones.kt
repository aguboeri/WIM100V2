package cl.dymcyber.wim100.datos.acceso

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.dominio.PonderadorWim
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.datos.bd.BaseWim
import cl.dymcyber.wim100.datos.bd.dao.FilaConsolidada
import cl.dymcyber.wim100.datos.bd.entidades.CriterioWim
import cl.dymcyber.wim100.datos.bd.entidades.DetalleValoracion
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import cl.dymcyber.wim100.datos.bd.entidades.Valoracion
import kotlinx.coroutines.flow.Flow

/** Se lanza cuando una evaluadora intenta valorar dos veces a la misma postulante. */
class ValoracionDuplicadaException(mensaje: String) : Exception(mensaje)

/** Se lanza cuando la postulación ya fue resuelta por Dirección. */
class PostulacionCerradaException(mensaje: String) : Exception(mensaje)

/** Reglas de datos de la evaluación. */
class RepositorioValoraciones(private val base: BaseWim) {

    private val valoracionDao = base.valoraciones()
    private val postulanteDao = base.postulantes()
    private val bitacoraDao = base.bitacora()

    fun observarDePostulante(postulanteId: Long): Flow<List<Valoracion>> =
        valoracionDao.observarDePostulante(postulanteId)

    fun observarConsolidado(): Flow<List<FilaConsolidada>> = valoracionDao.observarConsolidado()

    suspend fun leerDetalles(valoracionId: Long): List<DetalleValoracion> =
        valoracionDao.leerDetalles(valoracionId)

    suspend fun yaValoro(postulanteId: Long, credencial: String): Boolean =
        valoracionDao.contarDeEvaluadora(postulanteId, credencial) > 0

    /**
     * Registra una valoración completa dentro de una transacción.
     *
     * @param puntajesPorCriterio id del criterio asociado a su puntaje.
     * @throws ValoracionDuplicadaException si esta evaluadora ya la valoró.
     * @throws PostulacionCerradaException si Dirección ya resolvió el caso.
     * @return el puntaje ponderado que quedó registrado.
     */
    suspend fun registrar(
        postulanteId: Long,
        sesion: Sesion,
        matriz: List<CriterioWim>,
        puntajesPorCriterio: Map<Long, Int>,
        observacionesPorCriterio: Map<Long, String>,
        cierre: String
    ): Double {
        var nombrePostulante = ""
        try {
            return base.withTransaction {
                val postulante = postulanteDao.buscarPorId(postulanteId)
                    ?: throw IllegalStateException("La postulación ya no existe")
                nombrePostulante = postulante.nombre

                if (postulante.etapa.estaCerrada) {
                    throw PostulacionCerradaException("La postulación ya fue resuelta por Dirección")
                }

                // Primera barrera: preguntamos antes de escribir.
                if (valoracionDao.contarDeEvaluadora(postulanteId, sesion.credencial) > 0) {
                    throw ValoracionDuplicadaException("Ya registraste una valoración para ${postulante.nombre}")
                }

                val aportes = matriz.map { criterio ->
                    PonderadorWim.Aporte(
                        peso = criterio.peso,
                        puntaje = puntajesPorCriterio[criterio.id] ?: PonderadorWim.PUNTAJE_MINIMO
                    )
                }
                val total = PonderadorWim.consolidar(aportes)

                val valoracionId = try {
                    valoracionDao.insertar(
                        Valoracion(
                            postulanteId = postulanteId,
                            evaluadora = sesion.nombre,
                            credencialEvaluadora = sesion.credencial,
                            puntajeTotal = total,
                            cierre = cierre.trim()
                        )
                    )
                } catch (e: SQLiteConstraintException) {
                    // Segunda barrera: el índice único de la tabla.
                    throw ValoracionDuplicadaException("Ya registraste una valoración para ${postulante.nombre}")
                }

                valoracionDao.insertarDetalles(
                    matriz.map { criterio ->
                        DetalleValoracion(
                            valoracionId = valoracionId,
                            criterioId = criterio.id,
                            puntaje = puntajesPorCriterio[criterio.id] ?: PonderadorWim.PUNTAJE_MINIMO,
                            observacion = observacionesPorCriterio[criterio.id].orEmpty().trim(),
                            pesoAplicado = criterio.peso
                        )
                    }
                )

                val cantidad = valoracionDao.contarDePostulante(postulanteId)
                postulanteDao.cambiarEtapa(
                    postulanteId,
                    EtapaPostulacion.segunValoraciones(postulante.etapa, cantidad)
                )

                bitacoraDao.registrar(
                    EventoBitacora(
                        accion = EventoBitacora.VALORACION,
                        detalle = "${postulante.nombre}: puntaje $total",
                        autor = sesion.nombre,
                        perfil = sesion.perfil.rotulo,
                        postulanteId = postulanteId
                    )
                )

                total
            }
        } catch (duplicada: ValoracionDuplicadaException) {
            // Fuera de la transacción: dentro, el rollback la borraría.
            anotarIntentoDuplicado(sesion, nombrePostulante, postulanteId)
            throw duplicada
        }
    }

    private suspend fun anotarIntentoDuplicado(sesion: Sesion, nombre: String, postulanteId: Long) {
        bitacoraDao.registrar(
            EventoBitacora(
                accion = EventoBitacora.DUPLICADO,
                detalle = "Intento de segunda valoración sobre $nombre",
                autor = sesion.nombre,
                perfil = sesion.perfil.rotulo,
                postulanteId = postulanteId
            )
        )
    }
}
