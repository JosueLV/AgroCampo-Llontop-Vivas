package pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

import org.jetbrains.compose.resources.painterResource

import pe.edu.upeu.agrocampo_llontop_vivas.resources.Res
import pe.edu.upeu.agrocampo_llontop_vivas.resources.agrocampo_logo

private val VerdeAgro = Color(0xFF14563A)

@Composable
fun LoginPantalla(
    alIngresar: (String, String) -> Unit,
    avisoServidor: String? = null,
    habilitado: Boolean = true
) {

    var usuario by remember {
        mutableStateOf("")
    }

    var contrasena by remember {
        mutableStateOf("")
    }

    var errorLocal by remember {
        mutableStateOf<String?>(null)
    }

    val coloresCampos = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = VerdeAgro,
        focusedLabelColor = VerdeAgro,
        cursorColor = VerdeAgro
    )

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
            modifier = Modifier.size(125.dp)
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Nombre de la aplicación
        Text(
            text = "AgroCampo",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = VerdeAgro
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        // Autor del proyecto
        Text(
            text = "Llontop Vivas",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        // Campo de usuario
        OutlinedTextField(
            value = usuario,

            onValueChange = {
                usuario = it
                errorLocal = null
            },

            label = {
                Text("Usuario")
            },

            colors = coloresCampos,

            singleLine = true,

            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Campo de contraseña
        OutlinedTextField(
            value = contrasena,

            onValueChange = {
                contrasena = it
                errorLocal = null
            },

            label = {
                Text("Contraseña")
            },

            visualTransformation = PasswordVisualTransformation(),

            colors = coloresCampos,

            singleLine = true,

            modifier = Modifier.fillMaxWidth()
        )

        // Errores de validación
        errorLocal?.let { mensaje ->

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        // Mensajes del servidor
        avisoServidor?.let { mensaje ->

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // Botón de inicio de sesión
        Button(
            enabled = habilitado,

            onClick = {

                errorLocal = LoginValidador.validar(
                    usuario,
                    contrasena
                )

                if (errorLocal == null) {
                    alIngresar(
                        usuario.trim(),
                        contrasena
                    )
                }
            },

            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeAgro
            ),

            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {

            Text(
                text = "Iniciar sesión",
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}