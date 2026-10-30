package pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upeu.agrocampo_llontop_vivas.data.local.LotesLocal
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LotesRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.data.repository.LaboresRepositorioRemoto
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.SesionActual
import pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase.FiltrarLotesUseCase
import pe.edu.upeu.agrocampo_llontop_vivas.domain.usecase.FiltrosLotes

data class LotesUiState(
    val lotes: List<Lote> = emptyList(),
    val valles: List<String> = emptyList(),
    val valleSeleccionado: String = "",
    val soloAbiertas: Boolean = false,
    val busqueda: String = "",
    val sincronizando: Boolean = false,
    val pendientes: Int = 0,
    val pendientesPorLote: Map<Long, Int> = emptyMap(),
    val error: String? = null,
    val loteSeleccionado: Lote? = null,
    val labores: List<Labor> = emptyList(),
    val sincronizandoLabores: Boolean = false,
    val errorLabores: String? = null,
    val laborEditando: Labor? = null,
    val guardandoEdicion: Boolean = false,
    val errorEdicion: String? = null,
    val eliminandoLaborId: Long? = null,
    val cerrandoCampania: Boolean = false,
    val errorOperacion: String? = null
)

class LotesViewModel(
    private val local: LotesLocal,
    private val remoto: LotesRepositorioRemoto,
    private val laboresRemoto: LaboresRepositorioRemoto,
    private val sesionActual: SesionActual,
    private val filtrarLotes: FiltrarLotesUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(LotesUiState())
    val estado = _estado.asStateFlow()
    private var observacionLabores: Job? = null
    private val filtros = MutableStateFlow(FiltrosLotes())

    init {
        viewModelScope.launch {
            combine(local.observarLotes(), filtros) { lotes, filtro -> lotes to filtro }
                .collect { (lotes, filtro) ->
                    val valles = lotes.map { it.valle }.distinct().sorted()
                    val visibles = filtrarLotes(lotes, filtro)
                    _estado.update {
                        it.copy(
                            lotes = visibles,
                            valles = valles,
                            valleSeleccionado = filtro.valle,
                            soloAbiertas = filtro.soloAbiertas,
                            busqueda = filtro.busqueda
                        )
                    }
                }
        }
        viewModelScope.launch {
            local.observarNumeroPendientes().collect { pendientes ->
                _estado.update { it.copy(pendientes = pendientes) }
            }
        }
        viewModelScope.launch {
            local.observarPendientesPorLote().collect { conteos ->
                _estado.update { it.copy(pendientesPorLote = conteos) }
            }
        }
        actualizar()
    }

    /** Cambiar filtros no hace llamadas HTTP: se recalcula el flujo de SQLDelight. */
    fun seleccionarValle(valle: String) {
        filtros.update { it.copy(valle = valle) }
    }

    fun cambiarSoloAbiertas(activado: Boolean) {
        filtros.update { it.copy(soloAbiertas = activado) }
    }

    fun cambiarBusqueda(texto: String) {
        filtros.update { it.copy(busqueda = texto) }
    }

    /**
     * Reintenta en orden de creación. Si falla el servidor o la red, el
     * pendiente actual y los siguientes permanecen en SQLDelight.
     */
    private suspend fun enviarPendientes() {
        local.obtenerPendientes().forEach { pendiente ->
            val confirmada = laboresRemoto.crear(pendiente)
            local.confirmarLaborSincronizada(pendiente.id, confirmada)
        }
    }

    fun actualizar() {
        if (_estado.value.sincronizando || _estado.value.sincronizandoLabores) return

        viewModelScope.launch {
            _estado.update { it.copy(sincronizando = true, error = null) }
            try {
                enviarPendientes()
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
            it.copy(loteSeleccionado = lote, labores = emptyList(), errorLabores = null,
                laborEditando = null, guardandoEdicion = false, errorEdicion = null,
                errorOperacion = null)
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
            it.copy(loteSeleccionado = null, labores = emptyList(), errorLabores = null,
                laborEditando = null, guardandoEdicion = false, errorEdicion = null)
        }
    }

    fun actualizarLabores() {
        val loteId = _estado.value.loteSeleccionado?.id ?: return
        if (_estado.value.sincronizandoLabores || _estado.value.sincronizando) return

        viewModelScope.launch {
            _estado.update { it.copy(sincronizandoLabores = true, errorLabores = null) }
            try {
                enviarPendientes()
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

    /** El backend comprueba la autoría y el plazo; la interfaz comprueba el privilegio. */
    fun puedeEditarLabor(labor: Labor): Boolean {
        val lote = _estado.value.loteSeleccionado ?: return false
        return !lote.campaniaCerrada &&
            labor.loteId == lote.id &&
            !labor.pendiente &&
            labor.id > 0L &&
            sesionActual.tienePrivilegio("LABOR_EDITAR")
    }

    /** El servicio vuelve a verificar LABOR_ELIMINAR; ocultar el botón no es seguridad. */
    fun puedeEliminarLabor(labor: Labor): Boolean {
        val lote = _estado.value.loteSeleccionado ?: return false
        return labor.loteId == lote.id &&
            !labor.pendiente && labor.id > 0L &&
            sesionActual.tienePrivilegio("LABOR_ELIMINAR")
    }

    fun eliminarLabor(labor: Labor) {
        if (!puedeEliminarLabor(labor)) {
            _estado.update { it.copy(errorOperacion = "No tienes permiso para eliminar esta labor.") }
            return
        }
        if (_estado.value.eliminandoLaborId != null || _estado.value.cerrandoCampania) return
        viewModelScope.launch {
            _estado.update { it.copy(eliminandoLaborId = labor.id, errorOperacion = null) }
            try {
                laboresRemoto.eliminar(labor.id)
                local.eliminarLabor(labor.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update {
                    it.copy(errorOperacion = e.message ?: "No fue posible eliminar la labor.")
                }
            } finally {
                _estado.update { it.copy(eliminandoLaborId = null) }
            }
        }
    }

    fun puedeCerrarCampania(lote: Lote): Boolean =
        !lote.campaniaCerrada && sesionActual.tienePrivilegio("CAMPANIA_CERRAR")

    fun cerrarCampania() {
        val lote = _estado.value.loteSeleccionado ?: return
        if (!puedeCerrarCampania(lote)) {
            _estado.update { it.copy(errorOperacion = "No tienes permiso para cerrar la campaña.") }
            return
        }
        if (_estado.value.cerrandoCampania || _estado.value.eliminandoLaborId != null) return
        viewModelScope.launch {
            _estado.update { it.copy(cerrandoCampania = true, errorOperacion = null) }
            try {
                val cerrada = remoto.cerrarCampania(lote.id)
                local.guardarLotes(listOf(cerrada))
                _estado.update { it.copy(loteSeleccionado = cerrada, errorOperacion = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update {
                    it.copy(errorOperacion = e.message ?: "No se pudo cerrar la campaña.")
                }
            } finally {
                _estado.update { it.copy(cerrandoCampania = false) }
            }
        }
    }

    fun comenzarEdicion(labor: Labor) {
        if (!puedeEditarLabor(labor) || _estado.value.guardandoEdicion) return
        _estado.update { it.copy(laborEditando = labor, errorEdicion = null) }
    }

    fun cancelarEdicion() {
        if (_estado.value.guardandoEdicion) return
        _estado.update { it.copy(laborEditando = null, errorEdicion = null) }
    }

    /**
     * Devuelve un error solo si falla la validación local. Si es válida,
     * el diálogo permanece abierto hasta recibir confirmación HTTP.
     */
    fun guardarEdicion(
        tipo: TipoLabor,
        fecha: String,
        cantidadTexto: String,
        observacion: String
    ): String? = guardarEdicionConJornales(
        tipo, fecha, cantidadTexto,
        _estado.value.laborEditando?.cantidadJornales?.toString() ?: "1",
        observacion
    )

    /** Valida los jornales antes de enviar un PUT al backend. */
    fun guardarEdicionConJornales(
        tipo: TipoLabor,
        fecha: String,
        cantidadTexto: String,
        jornalesTexto: String,
        observacion: String
    ): String? {
        val labor = _estado.value.laborEditando ?: return "Selecciona una labor."
        if (_estado.value.guardandoEdicion) return "Ya se está guardando la corrección."
        if (!puedeEditarLabor(labor)) return "No tienes permiso para editar esta labor."

        val fechaFinal = fecha.trim()
        if (!Regex("""\d{4}-\d{2}-\d{2}""").matches(fechaFinal)) {
            return "Escribe la fecha AAAA-MM-DD."
        }
        val cantidad = cantidadTexto.trim().replace(',', '.').toDoubleOrNull()
        if (cantidad == null || !cantidad.isFinite() || cantidad <= 0.0) {
            return "La cantidad debe ser mayor que cero."
        }
        val jornales = jornalesTexto.trim().toIntOrNull()
        if (jornales == null || jornales < 1) {
            return "Los jornales deben ser un número entero mayor que cero."
        }
        if (observacion.length > 500) return "La observación admite máximo 500 caracteres."

        viewModelScope.launch {
            _estado.update { it.copy(guardandoEdicion = true, errorEdicion = null) }
            try {
                val actualizada = laboresRemoto.editar(
                    labor.copy(
                        tipo = tipo,
                        fecha = fechaFinal,
                        cantidad = cantidad,
                        cantidadJornales = jornales,
                        observacion = observacion.trim()
                    )
                )
                local.guardarLabores(listOf(actualizada))
                _estado.update {
                    it.copy(
                        laborEditando = null,
                        guardandoEdicion = false,
                        errorEdicion = null,
                        errorLabores = null
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.update {
                    it.copy(
                        guardandoEdicion = false,
                        errorEdicion = e.message ?: "No fue posible guardar la corrección."
                    )
                }
            }
        }
        return null
    }

    /**
     * Valida datos en la pantalla y almacena una labor pendiente sin requerir internet.
     * Devuelve null si el formulario es válido, o un mensaje visible si hay error.
     */
    fun registrarLaborPendiente(
        tipo: TipoLabor,
        fecha: String,
        cantidadTexto: String,
        observacion: String
    ): String? = registrarLaborPendienteConJornales(
        tipo, fecha, cantidadTexto, "1", observacion
    )

    /** Guarda jornales válidos en SQLDelight incluso si el dispositivo está sin conexión. */
    fun registrarLaborPendienteConJornales(
        tipo: TipoLabor,
        fecha: String,
        cantidadTexto: String,
        jornalesTexto: String,
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
        val jornales = jornalesTexto.trim().toIntOrNull()
        if (jornales == null || jornales < 1) {
            return "Los jornales deben ser un número entero mayor que cero."
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
                    observacion = observacion.trim(),
                    cantidadJornales = jornales
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
