package cl.dymcyber.wim100.core.reglas

/** Resultado de revisar un dato: correcto, o con un reparo que explicar. */
sealed interface Veredicto {

    /** El dato cumple todas las reglas. */
    data object Correcto : Veredicto

    /** El dato no cumple; [aviso] es el texto que leera la usuaria. */
    data class Reparo(val aviso: String) : Veredicto
}

/** Atajo para construir un reparo sin repetir el nombre de la clase. */
internal fun reparo(aviso: String): Veredicto = Veredicto.Reparo(aviso)

/** true cuando el dato paso la revision. */
val Veredicto.aprueba: Boolean
    get() = this is Veredicto.Correcto

/** El mensaje de error, o null si el dato esta correcto. */
val Veredicto.aviso: String?
    get() = (this as? Veredicto.Reparo)?.aviso
