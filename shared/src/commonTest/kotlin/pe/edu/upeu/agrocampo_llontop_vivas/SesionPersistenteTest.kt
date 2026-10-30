package pe.edu.upeu.agrocampo_llontop_vivas

import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.AlmacenSesionVolatil
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Sesion
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.SesionActual
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SesionPersistenteTest {
    private fun sesion() = Sesion(
        token = "jwt-firmado-ejemplo",
        usuario = "tecnico1",
        rol = "TECNICO",
        privilegios = setOf("LOTE_LEER", "LABOR_CREAR")
    )

    @Test
    fun restaurarSesionDesdeElMismoAlmacen() {
        val almacen = AlmacenSesionVolatil()
        SesionActual(almacen).abrir(sesion())

        val nuevaInstancia = SesionActual(almacen)
        assertEquals("tecnico1", nuevaInstancia.usuario)
        assertEquals("jwt-firmado-ejemplo", nuevaInstancia.token)
        assertTrue(nuevaInstancia.tienePrivilegio("LABOR_CREAR"))
    }

    @Test
    fun cerrarSesionLimpiaElAlmacenYElFlujoObservable() {
        val almacen = AlmacenSesionVolatil()
        val actual = SesionActual(almacen)
        actual.abrir(sesion())
        actual.cerrar()

        assertNull(actual.sesionFlujo.value)
        assertNull(SesionActual(almacen).token)
    }
}
