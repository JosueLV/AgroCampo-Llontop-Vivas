package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

/** Estado de la sesión y punto único para guardar o borrar las credenciales. */
class SesionActual(private val almacen: AlmacenSesion = AlmacenSesionVolatil()) {
    private var activa: Sesion? = almacen.recuperar()

    val sesion: Sesion?
        get() = activa

    val token: String?
        get() = activa?.token

    val usuario: String?
        get() = activa?.usuario

    fun tienePrivilegio(privilegio: String): Boolean =
        activa?.privilegios?.contains(privilegio) == true

    fun abrir(nuevaSesion: Sesion) {
        almacen.guardar(nuevaSesion)
        activa = nuevaSesion
    }

    fun cerrar() {
        almacen.borrar()
        activa = null
    }
}
