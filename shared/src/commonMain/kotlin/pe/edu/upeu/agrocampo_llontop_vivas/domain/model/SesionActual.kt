package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Estado observable de la sesión; el almacenamiento persistente es inyectado. */
class SesionActual(private val almacen: AlmacenSesion = AlmacenSesionVolatil()) {
    private val _sesionFlujo = MutableStateFlow(almacen.recuperar())
    val sesionFlujo: StateFlow<Sesion?> = _sesionFlujo

    val sesion: Sesion?
        get() = _sesionFlujo.value

    val token: String?
        get() = _sesionFlujo.value?.token

    val usuario: String?
        get() = _sesionFlujo.value?.usuario

    fun tienePrivilegio(privilegio: String): Boolean =
        _sesionFlujo.value?.privilegios?.contains(privilegio) == true

    fun abrir(nuevaSesion: Sesion) {
        almacen.guardar(nuevaSesion)
        _sesionFlujo.value = nuevaSesion
    }

    fun cerrar() {
        almacen.borrar()
        _sesionFlujo.value = null
    }
}
