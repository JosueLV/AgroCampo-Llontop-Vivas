package pe.edu.upeu.agrocampo_llontop_vivas.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

import pe.edu.upeu.agrocampo_llontop_vivas.data.remote.crearClienteHttp
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.SesionRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.SesionActual
import pe.edu.upeu.agrocampo_llontop_vivas.domain.repository.SesionRepositorio
import pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase.IniciarSesionUseCase
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginViewModel

val appModule = module {

    single { SesionActual() }

    // Un solo cliente HTTP con el token JWT actual para todas las solicitudes.
    single { crearClienteHttp(get()) }

    // Repositorio de autenticación
    single<SesionRepositorio> {
        SesionRepositorioRemoto(get())
    }

    // Caso de uso para iniciar sesión
    factory {
        IniciarSesionUseCase(get())
    }

    // ViewModel del login
    viewModelOf(::LoginViewModel)
}