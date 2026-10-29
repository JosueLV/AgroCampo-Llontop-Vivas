package pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion

object LoginValidador {

    fun validar(usuario: String, contrasena: String): String? {
        if (usuario.isBlank()) {
            return "Ingresa tu usuario"
        }

        if (contrasena.isBlank()) {
            return "Ingresa tu contraseña"
        }

        return null
    }
}