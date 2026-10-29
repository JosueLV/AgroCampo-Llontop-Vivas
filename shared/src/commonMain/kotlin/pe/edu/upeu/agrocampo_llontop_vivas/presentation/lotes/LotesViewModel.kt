package pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.agrocampo_llontop_vivas.data.local.LotesLocal
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LotesRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LaboresRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.SesionActual

data class LotesUiState(
    val lotes: List<Lote> = emptyList(),
    val sincronizando: Boolean = false,
    val error: String? = null,
    val loteSeleccionado: Lote? = null,
    val labores: List<Labor> = emptyList(),
    val sincronizandoLabores: Boolean = false,
    val errorLabores: String? = null
)

class LotesViewModel(
    private val local: LotesLocal,
    private val remoto: LotesRepositorioRemoto,
    private val laboresRemoto: LaboresRepositorioRemoto,
    private val sesionActual: SesionActual
) : ViewModel() {

    private val _estado = MutableStateFlow(LotesUiState())
    val estado = _estado.asStateFlow()
    private var observacionLabores: Job? = null

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
                _estado.update {
                    it.copy(error = e.message ?: "No se pudo actualizar. Se muestran los datos guardados.")
                }
            } finally {
                _estado.update { it.copy(sincronizando = false) }
            }
        }
    }

    fun abrirLote(lote: Lote) {
        observacionLabores?.cancel()
        _estado.update {
            it.copy(loteSeleccionado = lote, labores = emptyList(), errorLabores = null)
        }
        observacionLabores = viewModelScope.launch {
            local.observarLabores(lote.id).collect { labores ->
                _estado.update { actual ->
                    if (actual.loteSeleccionado?.id == lote.id) {
                        actual.copy(labores = labores)
                    } else actual
                }
            }
        }
        actualizarLabores()
    }

    fun volverAlListado() {
        observacionLabores?.cancel()
        _estado.update {
            it.copy(loteSeleccionado = null, labores = emptyList(), errorLabores = null)
        }
    }

    fun actualizarLabores() {
        val loteId = _estado.value.loteSeleccionado?.id ?: return
        if (_estado.value.sincronizandoLabores) return

        viewModelScope.launch {
            _estado.update { it.copy(sincronizandoLabores = true, errorLabores = null) }
            try {
                local.guardarLabores(laboresRemoto.listarPorLote(loteId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(errorLabores = e.message ?: "Sin conexión") }
            } finally {
                _estado.update { it.copy(sincronizandoLabores = false) }
            }
        }
    }

    fun puedeRegistrarEn(lote: Lote): Boolean =
        !lote.campaniaCerrada && sesionActual.tienePrivilegio("LABOR_CREAR")

    /**
     * Valida datos en la pantalla y almacena una labor pendiente sin requerir internet.
     * Devuelve null si el formulario es válido, o un mensaje visible si hay error.
     */
    fun registrarLaborPendiente(
        tipo: TipoLabor,
        fecha: String,
        cantidadTexto: String,
        observacion: String
    ): String? {
        val lote = _estado.value.loteSeleccionado ?: return "Selecciona un lote."
        if (!puedeRegistrarEn(lote)) return "No tienes permiso o la campaña está cerrada."
        val fechaFinal = fecha.trim()
        val formatoFecha = Regex("""\d{4}-\d{2}-\d{2}""")
        if (!formatoFecha.matches(fechaFinal)) return "Escribe la fecha AAAA-MM-DD."
        val cantidad = cantidadTexto.replace(',', '.').toDoubleOrNull()
        if (cantidad == null || !cantidad.isFinite() || cantidad <= 0.0) {
            return "La cantidad debe ser mayor que cero."
        }
        if (observacion.length > 500) return "La observación admite máximo 500 caracteres."
        val usuario = sesionActual.usuario ?: return "Debes iniciar sesión."

        viewModelScope.launch {
            try {
                local.crearLaborPendiente(
                    loteId = lote.id,
                    tipo = tipo,
                    fecha = fechaFinal,
                    responsable = usuario,
                    cantidad = cantidad,
                    observacion = observacion.trim()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update { it.copy(errorLabores = "No se pudo guardar localmente: ${e.message}") }
            }
        }
        return null
    }
}
