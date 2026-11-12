package cl.dymcyber.wim100.datos.bd

import cl.dymcyber.wim100.core.dominio.EtapaPostulacion
import cl.dymcyber.wim100.core.dominio.PonderadorWim
import cl.dymcyber.wim100.core.dominio.Sesion
import cl.dymcyber.wim100.datos.bd.entidades.CriterioWim
import cl.dymcyber.wim100.datos.bd.entidades.DetalleValoracion
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.datos.bd.entidades.Valoracion

/** Datos de arranque, todos ficticios. */
object PrecargaDemo {

    /** Matriz de ejemplo. */
    val MATRIZ = listOf(
        CriterioWim(
            nombre = "Experiencia técnica",
            descripcion = "Trayectoria comprobable en ciberseguridad o áreas afines.",
            peso = 25,
            orden = 1
        ),
        CriterioWim(
            nombre = "Formación y certificaciones",
            descripcion = "Estudios formales y certificaciones vigentes del rubro.",
            peso = 20,
            orden = 2
        ),
        CriterioWim(
            nombre = "Resolución de problemas",
            descripcion = "Capacidad de diagnosticar y responder ante incidentes.",
            peso = 20,
            orden = 3
        ),
        CriterioWim(
            nombre = "Comunicación y trabajo en equipo",
            descripcion = "Claridad al explicar riesgos a perfiles no técnicos.",
            peso = 20,
            orden = 4
        ),
        CriterioWim(
            nombre = "Compromiso con el programa",
            descripcion = "Disponibilidad y motivación declarada para WIM 100.",
            peso = 15,
            orden = 5
        )
    )

    /** Postulantes de muestra. */
    val POSTULANTES = listOf(
        Postulante(
            nombre = "Camila Soto",
            correo = "camila.soto@ejemplo.cl",
            telefono = "+56 9 1111 2222",
            especialidad = "Respuesta a incidentes",
            aniosTrayectoria = 6,
            sintesis = "Analista de un centro de operaciones de seguridad, con foco en contención de incidentes y manejo de evidencia.",
            perfilWeb = "https://www.linkedin.com/in/camila-soto-ficticia",
            registradaPor = "Equipo WIM"
        ),
        Postulante(
            nombre = "Valentina Rojas",
            correo = "valentina.rojas@ejemplo.cl",
            telefono = "+56 9 3333 4444",
            especialidad = "Seguridad ofensiva",
            aniosTrayectoria = 4,
            sintesis = "Realiza pruebas de intrusión en aplicaciones web y reporta hallazgos con planes de mitigación.",
            perfilWeb = "https://www.linkedin.com/in/valentina-rojas-ficticia",
            registradaPor = "Equipo WIM"
        ),
        Postulante(
            nombre = "Josefa Muñoz",
            correo = "josefa.munoz@example.com",
            telefono = "+56 9 5555 6666",
            especialidad = "Gobernanza y riesgo",
            aniosTrayectoria = 9,
            sintesis = "Lidera programas de cumplimiento y gestión de riesgo tecnológico en empresas de servicios.",
            perfilWeb = "https://www.linkedin.com/in/josefa-munoz-ficticia",
            registradaPor = "Equipo WIM"
        ),
        Postulante(
            nombre = "Antonia Fuentes",
            correo = "antonia.fuentes@test.cl",
            telefono = "+56 9 7777 8888",
            especialidad = "Seguridad en la nube",
            aniosTrayectoria = 3,
            sintesis = "Administra identidades y accesos en entornos de nube pública y automatiza controles básicos.",
            perfilWeb = "https://www.linkedin.com/in/antonia-fuentes-ficticia",
            registradaPor = "Equipo WIM"
        ),
        Postulante(
            nombre = "Daniela Herrera",
            correo = "daniela.herrera@ejemplo.cl",
            telefono = "+56 9 9999 0000",
            especialidad = "Análisis forense",
            aniosTrayectoria = 7,
            sintesis = "Perita evidencia digital y documenta cadenas de custodia para procesos internos.",
            perfilWeb = "https://www.linkedin.com/in/daniela-herrera-ficticia",
            registradaPor = "Equipo WIM"
        ),
        Postulante(
            nombre = "Martina Vega",
            correo = "martina.vega@ejemplo.cl",
            telefono = "+56 9 2468 1357",
            especialidad = "Concientización y formación",
            aniosTrayectoria = 2,
            sintesis = "Diseña campañas internas de concientización y mide su impacto con simulaciones de phishing.",
            perfilWeb = "https://www.linkedin.com/in/martina-vega-ficticia",
            registradaPor = "Equipo WIM"
        )
    )

    /** Evaluadoras ficticias que dejan valoraciones de ejemplo. */
    private val EVALUADORAS = listOf("Paula Lagos", "Ignacia Bravo")

    /** Llena la base solo si está vacía. */
    suspend fun sembrar(base: BaseWim) {
        if (base.criterios().contar() > 0 || base.postulantes().contarTodas() > 0) return

        base.criterios().insertarTodos(MATRIZ)
        val matriz = base.criterios().leerMatriz()

        val idsPostulantes = POSTULANTES.map { base.postulantes().insertar(it) }

        val puntajesDemo = listOf(
            listOf(5, 4, 5, 4, 5),
            listOf(4, 4, 4, 5, 4),
            listOf(3, 4, 3, 4, 4),
            listOf(4, 3, 4, 3, 4)
        )
        var fila = 0
        idsPostulantes.take(2).forEach { postulanteId ->
            EVALUADORAS.forEach { evaluadora ->
                val puntajes = puntajesDemo[fila % puntajesDemo.size]
                fila++
                val aportes = matriz.mapIndexed { i, criterio ->
                    PonderadorWim.Aporte(peso = criterio.peso, puntaje = puntajes[i])
                }
                val total = PonderadorWim.consolidar(aportes)
                val valoracionId = base.valoraciones().insertar(
                    Valoracion(
                        postulanteId = postulanteId,
                        evaluadora = evaluadora,
                        credencialEvaluadora = Sesion.acreditar(evaluadora),
                        puntajeTotal = total,
                        cierre = "Perfil consistente con los criterios del programa WIM 100."
                    )
                )
                base.valoraciones().insertarDetalles(
                    matriz.mapIndexed { i, criterio ->
                        DetalleValoracion(
                            valoracionId = valoracionId,
                            criterioId = criterio.id,
                            puntaje = puntajes[i],
                            observacion = "",
                            pesoAplicado = criterio.peso
                        )
                    }
                )
                base.bitacora().registrar(
                    EventoBitacora(
                        accion = EventoBitacora.VALORACION,
                        detalle = "Valoración de ejemplo con puntaje $total",
                        autor = evaluadora,
                        perfil = "Evaluadora",
                        postulanteId = postulanteId
                    )
                )
            }
            base.postulantes().cambiarEtapa(postulanteId, EtapaPostulacion.EVALUADA)
        }
    }
}
