package cl.dymcyber.wim100.funciones

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import cl.dymcyber.wim100.AplicacionWim
import cl.dymcyber.wim100.Dependencias
import cl.dymcyber.wim100.funciones.acceso.AccesoVm
import cl.dymcyber.wim100.funciones.bitacora.BitacoraVm
import cl.dymcyber.wim100.funciones.postulantes.FichaVm
import cl.dymcyber.wim100.funciones.postulantes.FormularioVm
import cl.dymcyber.wim100.funciones.postulantes.ListaVm
import cl.dymcyber.wim100.funciones.resultados.ResultadosVm
import cl.dymcyber.wim100.funciones.valoracion.ValoracionVm

/** Fábrica única de ViewModels. */
object FabricaVm {

    val Reglas = viewModelFactory {
        initializer { AccesoVm(dependencias()) }
        initializer { ListaVm(dependencias()) }
        initializer { FormularioVm(dependencias()) }
        initializer { FichaVm(createSavedStateHandle(), dependencias()) }
        initializer { ValoracionVm(createSavedStateHandle(), dependencias()) }
        initializer { ResultadosVm(dependencias()) }
        initializer { BitacoraVm(dependencias()) }
    }
}

/** Atajo para alcanzar el contenedor desde dentro de la fábrica. */
private fun CreationExtras.dependencias(): Dependencias =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AplicacionWim).dependencias
