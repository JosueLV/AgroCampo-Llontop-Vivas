package pe.edu.upeu.agrocampo_llontop_vivas

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import org.jetbrains.compose.resources.painterResource

import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

import pe.edu.upeu.agrocampo_llontop_vivas.di.appModule
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginPantalla
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginViewModel

import pe.edu.upeu.agrocampo_llontop_vivas.resources.Res
import pe.edu.upeu.agrocampo_llontop_vivas.resources.agrocampo_logo

private val VerdeAgro = Color(0xFF14563A)

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

                // Pantalla de inicio de sesión
                LoginPantalla(
                    alIngresar = viewModel::ingresar,
                    habilitado = !estado.cargando,
                    avisoServidor = when {
                        estado.cargando -> "Conectando con AgroAPI..."
                        else -> estado.error
                    }
                )

            } else {

                // Pantalla de bienvenida
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),

                    verticalArrangement = Arrangement.Center,

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Logotipo de AgroCampo
                    Image(
                        painter = painterResource(
                            Res.drawable.agrocampo_logo
                        ),
                        contentDescription = "Logotipo AgroCampo",
                        modifier = Modifier.size(150.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    // Mensaje principal
                    Text(
                        text = "Bienvenido a AgroCampo",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = VerdeAgro
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    // Datos del usuario autenticado
                    Text(
                        text = "Usuario: ${sesion.usuario}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "Rol: ${sesion.rol}",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(
                        modifier = Modifier.height(24.dp)
                    )

                    // Cerrar sesión
                    Button(
                        onClick = viewModel::cerrarSesion,

                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeAgro
                        )
                    ) {
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    }
}