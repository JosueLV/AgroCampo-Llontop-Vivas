package pe.edu.upeu.agrocampo_llontop_vivas.domain.model

/** Puerto común de persistencia; Android proporciona una implementación cifrada. */
interface AlmacenSesion {
    fun recuperar(): Sesion?
    fun guardar(sesion: Sesion)
    fun borrar()
}

/** Implementación ligera para pruebas y el destino iOS de ejemplo. */
class AlmacenSesionVolatil : AlmacenSesion {
    private var valor: Sesion? = null
    override fun recuperar(): Sesion? = valor
    override fun guardar(sesion: Sesion) { valor = sesion }
    override fun borrar() { valor = null }
}
