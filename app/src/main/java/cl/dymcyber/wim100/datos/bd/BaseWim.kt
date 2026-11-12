package cl.dymcyber.wim100.datos.bd

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import cl.dymcyber.wim100.datos.bd.dao.BitacoraDao
import cl.dymcyber.wim100.datos.bd.dao.MatrizDao
import cl.dymcyber.wim100.datos.bd.dao.PostulanteDao
import cl.dymcyber.wim100.datos.bd.dao.ValoracionDao
import cl.dymcyber.wim100.datos.bd.entidades.CriterioWim
import cl.dymcyber.wim100.datos.bd.entidades.DetalleValoracion
import cl.dymcyber.wim100.datos.bd.entidades.EventoBitacora
import cl.dymcyber.wim100.datos.bd.entidades.Postulante
import cl.dymcyber.wim100.datos.bd.entidades.Valoracion

/** Base de datos local de la aplicación, en el archivo wim100.db. */
@Database(
    entities = [
        Postulante::class,
        CriterioWim::class,
        Valoracion::class,
        DetalleValoracion::class,
        EventoBitacora::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Conversores::class)
abstract class BaseWim : RoomDatabase() {

    abstract fun postulantes(): PostulanteDao
    abstract fun criterios(): MatrizDao
    abstract fun valoraciones(): ValoracionDao
    abstract fun bitacora(): BitacoraDao

    companion object {
        private const val ARCHIVO = "wim100.db"

        @Volatile
        private var instancia: BaseWim? = null

        fun obtener(contexto: Context): BaseWim =
            instancia ?: synchronized(this) {
                instancia ?: construir(contexto).also { instancia = it }
            }

        private fun construir(contexto: Context): BaseWim =
            Room.databaseBuilder(contexto.applicationContext, BaseWim::class.java, ARCHIVO)
                // MVP académico: al subir la versión se recrean las tablas.
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
