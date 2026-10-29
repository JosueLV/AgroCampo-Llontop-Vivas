package pe.edu.upeu.agrocampo_llontop_vivas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

import pe.edu.upeu.agrocampo_llontop_vivas.di.appModule
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginPantalla
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginViewModel

@Composable
fun App() {

    KoinApplication(
        configuration = koinConfiguration {
            modules(appModule)
        }
    ) {

        val viewModel = koinViewModel<LoginViewModel>()

        val estado by viewModel.estado.collectAsState()

        val sesion = estado.sesion

        MaterialTheme {

            if (sesion == null) {

                LoginPantalla(
                    alIngresar = viewModel::ingresar,
                    habilitado = !estado.cargando,
                    avisoServidor = when {
                        estado.cargando -> "Conectando con AgroAPI..."
                        else -> estado.error
                    }
                )

            } else {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    verticalArrangement = Arrangement.Center,

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Bienvenido a AgroCampo",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "Usuario: ${sesion.usuario}"
                    )

                    Text(
                        text = "Rol: ${sesion.rol}"
                    )

                    Button(
                        onClick = viewModel::cerrarSesion
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }
}