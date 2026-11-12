package cl.dymcyber.wim100.core.reglas

/** Reglas del formulario de postulación. */
object ReglasPostulante {

    const val NOMBRE_LARGO_MINIMO = 3
    const val NOMBRE_LARGO_MAXIMO = 60
    const val TRAYECTORIA_MAXIMA_ANIOS = 50
    const val SINTESIS_LARGO_MINIMO = 30
    const val SINTESIS_LARGO_MAXIMO = 500
    const val CV_PESO_MAXIMO_BYTES = 5L * 1024 * 1024

    /** El caso prohibe datos reales, así que solo se aceptan dominios de prueba. */
    val DOMINIOS_DE_PRUEBA = listOf("ejemplo.cl", "example.com", "test.cl")

    private val SOLO_LETRAS = Regex("^[\\p{L} ']+$")
    private val FORMA_CORREO = Regex("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")
    private val NUEVE_DIGITOS_CHILENOS = Regex("^(56)?9\\d{8}$")
    private val PERFIL_PROFESIONAL = Regex(
        "^https://(www\\.)?linkedin\\.com/in/[\\w-]{3,100}/?$",
        RegexOption.IGNORE_CASE
    )

    fun nombre(entrada: String): Veredicto {
        val texto = entrada.trim()
        if (texto.isEmpty()) return reparo("Escribe el nombre de la postulante")
        if (texto.length < NOMBRE_LARGO_MINIMO) return reparo("Usa al menos $NOMBRE_LARGO_MINIMO caracteres")
        if (texto.length > NOMBRE_LARGO_MAXIMO) return reparo("No superes los $NOMBRE_LARGO_MAXIMO caracteres")
        if (!SOLO_LETRAS.matches(texto)) return reparo("Solo se aceptan letras y espacios")
        if (texto.split(" ").filter { it.isNotBlank() }.size < 2) return reparo("Falta el apellido")
        return Veredicto.Correcto
    }

    fun correo(entrada: String): Veredicto {
        val texto = entrada.trim()
        if (texto.isEmpty()) return reparo("El correo de contacto es obligatorio")
        if (!FORMA_CORREO.matches(texto)) return reparo("Revisa el formato del correo")
        val dominio = texto.substringAfterLast('@').lowercase()
        if (dominio !in DOMINIOS_DE_PRUEBA) {
            return reparo("Usa un dominio de prueba: ${DOMINIOS_DE_PRUEBA.joinToString(", ")}")
        }
        return Veredicto.Correcto
    }

    fun telefono(entrada: String): Veredicto {
        if (entrada.isBlank()) return reparo("El teléfono de contacto es obligatorio")
        val permitidos = entrada.all { it.isDigit() || it in " +-" }
        if (!permitidos) return reparo("Solo números, espacios, + y guiones")
        val digitos = entrada.filter { it.isDigit() }
        if (!NUEVE_DIGITOS_CHILENOS.matches(digitos)) return reparo("Formato esperado: +56 9 1234 5678")
        return Veredicto.Correcto
    }

    fun especialidad(entrada: String?): Veredicto =
        if (entrada.isNullOrBlank()) reparo("Elige el área de especialidad") else Veredicto.Correcto

    fun trayectoria(entrada: String): Veredicto {
        if (entrada.isBlank()) return reparo("Indica los años de experiencia")
        val anios = entrada.trim().toIntOrNull() ?: return reparo("Escribe un número entero")
        if (anios < 0) return reparo("No puede ser negativo")
        if (anios > TRAYECTORIA_MAXIMA_ANIOS) return reparo("Máximo $TRAYECTORIA_MAXIMA_ANIOS años")
        return Veredicto.Correcto
    }

    fun sintesis(entrada: String): Veredicto {
        val texto = entrada.trim()
        if (texto.isEmpty()) return reparo("Describe la trayectoria de la postulante")
        if (texto.length < SINTESIS_LARGO_MINIMO) {
            return reparo("Faltan ${SINTESIS_LARGO_MINIMO - texto.length} caracteres")
        }
        if (texto.length > SINTESIS_LARGO_MAXIMO) return reparo("Máximo $SINTESIS_LARGO_MAXIMO caracteres")
        return Veredicto.Correcto
    }

    fun perfilWeb(entrada: String): Veredicto {
        val texto = entrada.trim()
        if (texto.isEmpty()) return reparo("El enlace al perfil profesional es obligatorio")
        if (!texto.startsWith("https://", ignoreCase = true)) return reparo("El enlace debe partir con https://")
        if (!PERFIL_PROFESIONAL.matches(texto)) {
            return reparo("Formato esperado: https://www.linkedin.com/in/perfil-ficticio")
        }
        return Veredicto.Correcto
    }

    fun curriculum(nombreArchivo: String?, pesoBytes: Long?): Veredicto {
        if (nombreArchivo.isNullOrBlank()) return reparo("Adjunta el CV de prueba en PDF")
        if (!nombreArchivo.endsWith(".pdf", ignoreCase = true)) return reparo("El archivo debe ser un PDF")
        if (pesoBytes != null && pesoBytes > CV_PESO_MAXIMO_BYTES) return reparo("El PDF supera los 5 MB")
        return Veredicto.Correcto
    }

    /** Deja "+56912345678" o "9 1234 5678" como "+56 9 1234 5678". */
    fun ordenarTelefono(entrada: String): String {
        val d = entrada.filter { it.isDigit() }.removePrefix("56")
        if (d.length != 9) return entrada.trim()
        return "+56 ${d.take(1)} ${d.substring(1, 5)} ${d.substring(5)}"
    }
}
