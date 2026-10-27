package pe.edu.upeu.agrocampo_llontop_vivas

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion.LoginPantalla

@Composable
fun App() {
    var aviso by remember { mutableStateOf<String?>(null) }

    MaterialTheme {
        LoginPantalla(
            alIngresar = { _, _ ->
                aviso = "Conectaremos el backend en el siguiente paso"
            },
            avisoServidor = aviso
        )
    }
}