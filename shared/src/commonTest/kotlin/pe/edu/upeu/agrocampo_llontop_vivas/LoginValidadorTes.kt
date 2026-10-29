package pe.edu.upeu.agrocampo_llontop_vivas

import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginValidador
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoginValidadorTest {

    @Test
    fun usuarioVacioMuestraError() {
        val resultado = LoginValidador.validar("", "Agro2026*")

        assertEquals("Ingresa tu usuario", resultado)
    }

    @Test
    fun contrasenaVaciaMuestraError() {
        val resultado = LoginValidador.validar("tecnico1", "")

        assertEquals("Ingresa tu contraseña", resultado)
    }

    @Test
    fun camposValidosPermitenContinuar() {
        val resultado = LoginValidador.validar("tecnico1", "Agro2026*")

        assertNull(resultado)
    }
}
