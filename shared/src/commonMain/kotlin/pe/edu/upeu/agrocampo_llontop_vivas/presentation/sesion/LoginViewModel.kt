package pe.edu.upeu.agrocampo_llontop_vivas.presentation.sesion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Sesion
import pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase.IniciarSesionUseCase

data class LoginUiState(
    val cargando: Boolean = false,
    val sesion: Sesion? = null,
    val error: String? = null
)

class LoginViewModel(
    private val iniciarSesion: IniciarSesionUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado = _estado.asStateFlow()

    fun ingresar(usuario: String, contrasena: String) {
        viewModelScope.launch {
            _estado.value = LoginUiState(cargando = true)

            try {
                val sesion = iniciarSesion(usuario, contrasena)
                _estado.value = LoginUiState(sesion = sesion)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = LoginUiState(
                    error = e.message ?: "No se pudo iniciar sesión"
                )
            }
        }
    }

    fun cerrarSesion() {
        _estado.value = LoginUiState()
    }
}