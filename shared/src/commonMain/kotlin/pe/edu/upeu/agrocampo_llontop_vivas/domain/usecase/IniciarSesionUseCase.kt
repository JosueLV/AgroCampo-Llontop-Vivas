package pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase

import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Sesion
import pe.edu.upeu.agrocampo_llontop_vivas.domain.repository.SesionRepositorio

class IniciarSesionUseCase(
    private val repositorio: SesionRepositorio
) {
    suspend operator fun invoke(
        usuario: String,
        contrasena: String
    ): Sesion {
        return repositorio.iniciarSesion(usuario, contrasena)
    }
}