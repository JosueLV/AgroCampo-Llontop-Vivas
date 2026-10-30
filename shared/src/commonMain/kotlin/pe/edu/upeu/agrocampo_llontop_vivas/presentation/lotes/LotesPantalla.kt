package pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Labor
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.TipoLabor
import pe.edu.upeu.agrocampo_llontop_vivas.resources.Res
import pe.edu.upeu.agrocampo_llontop_vivas.resources.agrocampo_logo

private val VerdeAgro = Color(0xFF14563A)

@Composable
fun LotesPantalla(
    usuario: String,
    rol: String,
    alCerrarSesion: () -> Unit,
    viewModel: LotesViewModel = koinViewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }

    if (mostrarFormulario && estado.loteSeleccionado != null) {
        NuevaLaborDialog(
            alCerrar = { mostrarFormulario = false },
            alGuardar = viewModel::registrarLaborPendienteConJornales
        )
    }

    estado.laborEditando?.let { labor ->
        EditarLaborDialog(
            labor = labor,
            guardando = estado.guardandoEdicion,
            errorServidor = estado.errorEdicion,
            alCerrar = viewModel::cancelarEdicion,
            alGuardar = viewModel::guardarEdicionConJornales
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.agrocampo_logo),
                contentDescription = "Logotipo AgroCampo",
                modifier = Modifier.size(70.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("AgroCampo", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = VerdeAgro)
                Text("$usuario · $rol", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = alCerrarSesion) {
                Text("Salir")
            }
        }

        if (estado.pendientes > 0) {
            Text(
                "Labores pendientes de envío: ${estado.pendientes}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        val seleccionado = estado.loteSeleccionado
        if (seleccionado == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Lotes de cultivo", style = MaterialTheme.typography.titleLarge)
                Button(
                    onClick = viewModel::actualizar,
                    enabled = !estado.sincronizando,
                    colors = ButtonDefaults.buttonColors(containerColor = VerdeAgro)
                ) {
                    Text(if (estado.pendientes > 0) "Sincronizar (${estado.pendientes})" else "Actualizar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            ControlesFiltrosLotes(
                estado = estado,
                alBuscar = viewModel::cambiarBusqueda,
                alElegirValle = viewModel::seleccionarValle,
                alCambiarAbiertas = viewModel::cambiarSoloAbiertas
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (estado.sincronizando) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Text("  Sincronizando lotes...")
                }
            }
            estado.error?.let { error ->
                Text(error, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error)
            }
            if (estado.lotes.isEmpty() && !estado.sincronizando) {
                val hayFiltro = estado.busqueda.isNotBlank() ||
                    estado.valleSeleccionado.isNotBlank() || estado.soloAbiertas
                Text(
                    if (hayFiltro) "Ningún lote coincide con los filtros seleccionados."
                    else "No hay lotes guardados. Conecta con AgroAPI y pulsa Actualizar.",
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.lotes, key = { it.id }) { lote ->
                    TarjetaLote(
                        lote = lote,
                        pendientes = estado.pendientesPorLote[lote.id] ?: 0,
                        alSeleccionar = { viewModel.abrirLote(lote) }
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Labores del lote", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = {
                    mostrarFormulario = false
                    viewModel.volverAlListado()
                }) {
                    Text("Volver")
                }
            }
            Text("${seleccionado.codigo} · ${seleccionado.nombre}",
                style = MaterialTheme.typography.titleMedium, color = VerdeAgro)
            Text("Valle: ${seleccionado.valle}  |  Cultivo: ${seleccionado.cultivo}")
            Spacer(modifier = Modifier.height(10.dp))

            val puedeRegistrar = viewModel.puedeRegistrarEn(seleccionado)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = viewModel::actualizarLabores,
                    enabled = !estado.sincronizandoLabores,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(if (estado.pendientes > 0) "↻  Sincronizar (${estado.pendientes})" else "↻  Actualizar", color = VerdeAgro)
                }
                if (puedeRegistrar) {
                    Button(
                        onClick = { mostrarFormulario = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeAgro)
                    ) {
                        Text("＋  Nueva labor")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (estado.sincronizandoLabores) {
                Text("Consultando AgroAPI...")
            }
            estado.errorLabores?.let { error ->
                Text("Datos locales disponibles. $error",
                    color = MaterialTheme.colorScheme.error)
            }
            if (estado.labores.isEmpty() && !estado.sincronizandoLabores) {
                Text("Este lote todavía no tiene labores guardadas.",
                    modifier = Modifier.padding(top = 12.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.labores, key = { it.id }) { labor ->
                    TarjetaLabor(
                        labor = labor,
                        puedeEditar = viewModel.puedeEditarLabor(labor),
                        alEditar = { viewModel.comenzarEdicion(labor) }
                    )
                }
            }
        }
    }
}

/**
 * Solo presenta los controles. La selección se procesa en el ViewModel y el
 * filtrado real sucede en FiltrarLotesUseCase sobre el flujo de SQLDelight.
 */
@Composable
private fun ControlesFiltrosLotes(
    estado: LotesUiState,
    alBuscar: (String) -> Unit,
    alElegirValle: (String) -> Unit,
    alCambiarAbiertas: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = estado.busqueda,
        onValueChange = alBuscar,
        label = { Text("Buscar lote por nombre o código") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = estado.soloAbiertas,
            onCheckedChange = alCambiarAbiertas
        )
        Text("Solo campañas abiertas", style = MaterialTheme.typography.bodyMedium)
    }
    Text("Valle", style = MaterialTheme.typography.labelMedium)
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = estado.valleSeleccionado.isEmpty(),
            onClick = { alElegirValle("") },
            label = { Text("Todos") }
        )
        estado.valles.forEach { valle ->
            FilterChip(
                selected = estado.valleSeleccionado == valle,
                onClick = { alElegirValle(valle) },
                label = { Text(valle) }
            )
        }
    }
}

@Composable
private fun TarjetaLote(lote: Lote, pendientes: Int, alSeleccionar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = alSeleccionar)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("${lote.codigo} · ${lote.nombre}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Cultivo: ${lote.cultivo}  |  Valle: ${lote.valle}")
            Text("Superficie: ${lote.hectareas} ha")
            Text(
                if (lote.campaniaCerrada) "Campaña cerrada" else "Campaña abierta",
                color = if (lote.campaniaCerrada) MaterialTheme.colorScheme.error else VerdeAgro
            )
            if (pendientes > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = "Pendientes: $pendientes",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text("Toca para ver labores", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TarjetaLabor(
    labor: Labor,
    puedeEditar: Boolean,
    alEditar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("${labor.tipo.name} · ${labor.fecha}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Responsable: ${labor.responsable}")
            Text("Cantidad: ${labor.cantidad}")
            Text("Jornales: ${labor.cantidadJornales}")
            if (labor.observacion.isNotBlank()) {
                Text("Observación: ${labor.observacion}")
            }
            if (labor.pendiente) {
                Text("Pendiente de sincronizar", color = MaterialTheme.colorScheme.error)
            }
            if (puedeEditar) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = alEditar,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("✎  Editar")
                }
            }
        }
    }
}

@Composable
private fun NuevaLaborDialog(
    alCerrar: () -> Unit,
    alGuardar: (TipoLabor, String, String, String, String) -> String?
) {
    var tipo by remember { mutableStateOf(TipoLabor.RIEGO) }
    var fecha by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var jornales by remember { mutableStateOf("1") }
    var observacion by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Registrar labor pendiente") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Tipo de labor", style = MaterialTheme.typography.titleSmall)
                TipoLabor.entries.forEach { opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipo == opcion,
                            onClick = { tipo = opcion; error = null }
                        )
                        Text(opcion.name)
                    }
                }
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it; error = null },
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it; error = null },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jornales,
                    onValueChange = { jornales = it; error = null },
                    label = { Text("Cantidad de jornales") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it; error = null },
                    label = { Text("Observación (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Se guardará localmente como pendiente hasta sincronizar.",
                    style = MaterialTheme.typography.bodySmall)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = alGuardar(tipo, fecha, cantidad, jornales, observacion)
                    if (error == null) alCerrar()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VerdeAgro)
            ) {
                Text("Guardar pendiente")
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) { Text("Cancelar") }
        }
    )
}

/** Conserva el formulario abierto hasta que el backend confirma la corrección. */
@Composable
private fun EditarLaborDialog(
    labor: Labor,
    guardando: Boolean,
    errorServidor: String?,
    alCerrar: () -> Unit,
    alGuardar: (TipoLabor, String, String, String, String) -> String?
) {
    var tipo by remember(labor.id) { mutableStateOf(labor.tipo) }
    var fecha by remember(labor.id) { mutableStateOf(labor.fecha) }
    var cantidad by remember(labor.id) { mutableStateOf(labor.cantidad.toString()) }
    var jornales by remember(labor.id) { mutableStateOf(labor.cantidadJornales.toString()) }
    var observacion by remember(labor.id) { mutableStateOf(labor.observacion) }
    var errorLocal by remember(labor.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!guardando) alCerrar() },
        title = { Text("Editar labor #${labor.id}") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Tipo de labor", style = MaterialTheme.typography.titleSmall)
                TipoLabor.entries.forEach { opcion ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipo == opcion,
                            enabled = !guardando,
                            onClick = { tipo = opcion; errorLocal = null }
                        )
                        Text(opcion.name)
                    }
                }
                OutlinedTextField(
                    value = fecha,
                    onValueChange = { fecha = it; errorLocal = null },
                    enabled = !guardando,
                    label = { Text("Fecha (AAAA-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it; errorLocal = null },
                    enabled = !guardando,
                    label = { Text("Cantidad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = jornales,
                    onValueChange = { jornales = it; errorLocal = null },
                    enabled = !guardando,
                    label = { Text("Cantidad de jornales") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = observacion,
                    onValueChange = { observacion = it; errorLocal = null },
                    enabled = !guardando,
                    label = { Text("Observación (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorLocal?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                errorServidor?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                if (guardando) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("  Guardando en AgroAPI...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorLocal = alGuardar(tipo, fecha, cantidad, jornales, observacion)
                },
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = VerdeAgro)
            ) {
                Text("Guardar cambios")
            }
        },
        dismissButton = {
            TextButton(
                onClick = alCerrar,
                enabled = !guardando
            ) {
                Text("Cancelar")
            }
        }
    )
}
