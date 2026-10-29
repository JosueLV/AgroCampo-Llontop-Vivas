package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

/**
 * Sesión activa en memoria. El almacenamiento cifrado se integrará en una etapa posterior.
 */
class SesionActual {
    private var sesion: Sesion? = null

    val token: String?
        get() = sesion?.token

    fun abrir(nuevaSesion: Sesion) {
        sesion = nuevaSesion
    }

    fun cerrar() {
        sesion = null
    }
}
