package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

/**
 * Sesión activa en memoria. El almacenamiento cifrado se integrará en una etapa posterior.
 */
class SesionActual {
    private var sesion: Sesion? = null

    val token: String?
        get() = sesion?.token

    val usuario: String?
        get() = sesion?.usuario

    fun tienePrivilegio(privilegio: String): Boolean =
        sesion?.privilegios?.contains(privilegio) == true

    fun abrir(nuevaSesion: Sesion) {
        sesion = nuevaSesion
    }

    fun cerrar() {
        sesion = null
    }
}
