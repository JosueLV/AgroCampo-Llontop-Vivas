package pe.edu.upeu.agrocampo_llontop_vivas.domain.repository

import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Sesion

interface SesionRepositorio {
    suspend fun iniciarSesion(
        usuario: String,
        contrasena: String
    ): Sesion
}