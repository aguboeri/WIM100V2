package cl.dymcyber.wim100.datos.bd

import androidx.room.TypeConverter
import cl.dymcyber.wim100.core.dominio.EtapaPostulacion

/** SQLite solo guarda tipos simples. */
class Conversores {

    @TypeConverter
    fun etapaATexto(etapa: EtapaPostulacion): String = etapa.name

    @TypeConverter
    fun textoAEtapa(texto: String): EtapaPostulacion =
        runCatching { EtapaPostulacion.valueOf(texto) }.getOrDefault(EtapaPostulacion.RECIBIDA)
}
