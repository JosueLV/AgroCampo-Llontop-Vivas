package pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.agrocampo_llontop_vivas.data.local.LotesLocal
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LotesRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote

data class LotesUiState(
    val lotes: List<Lote> = emptyList(),
    val sincronizando: Boolean = false,
    val error: String? = null
)

class LotesViewModel(
    private val local: LotesLocal,
    private val remoto: LotesRepositorioRemoto
) : ViewModel() {

    private val _estado = MutableStateFlow(LotesUiState())
    val estado = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            local.observarLotes().collect { lotes ->
                _estado.update { it.copy(lotes = lotes) }
            }
        }
        actualizar()
    }

    fun actualizar() {
        if (_estado.value.sincronizando) return

        viewModelScope.launch {
            _estado.update { it.copy(sincronizando = true, error = null) }
            try {
                local.guardarLotes(remoto.listarTodos())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Conservamos los lotes locales cuando no hay internet.
                _estado.update {
                    it.copy(error = e.message ?: "No se pudo actualizar. Se muestran los datos guardados.")
                }
            } finally {
                _estado.update { it.copy(sincronizando = false) }
            }
        }
    }
}
