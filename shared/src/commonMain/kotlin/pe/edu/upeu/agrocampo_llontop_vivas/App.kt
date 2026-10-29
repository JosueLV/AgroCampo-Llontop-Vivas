package pe.edu.upeu.agrocampo_llontop_vivas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.cash.sqldelight.db.SqlDriver
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration
import pe.edu.upeu.agrocampo_llontop_vivas.di.appModule
import pe.edu.upeu.agrocampo_llontop_vivas.di.crearModuloLotes
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes.LotesPantalla
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginPantalla
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginViewModel

@Composable
fun App(driver: SqlDriver) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appModule, crearModuloLotes(driver))
        }
    ) {
        val loginViewModel = koinViewModel<LoginViewModel>()
        val estado by loginViewModel.estado.collectAsState()

        MaterialTheme {
            Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            val sesion = estado.sesion
            if (sesion == null) {
                LoginPantalla(
                    alIngresar = loginViewModel::ingresar,
                    habilitado = !estado.cargando,
                    avisoServidor = when {
                        estado.cargando -> "Conectando con AgroAPI..."
                        else -> estado.error
                    }
                )
            } else {
                LotesPantalla(
                    usuario = sesion.usuario,
                    rol = sesion.rol,
                    alCerrarSesion = loginViewModel::cerrarSesion
                )
            }
            }
        }
    }
}
