package pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes

import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
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
            alGuardar = viewModel::registrarLaborPendiente
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
                    Text("Actualizar")
                }
            }

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
                Text("No hay lotes guardados. Conecta con AgroAPI y pulsa Actualizar.",
                    modifier = Modifier.padding(top = 24.dp))
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(estado.lotes, key = { it.id }) { lote ->
                    TarjetaLote(lote, alSeleccionar = { viewModel.abrirLote(lote) })
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
                    Text("↻  Actualizar", color = VerdeAgro)
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
                    TarjetaLabor(labor)
                }
            }
        }
    }
}

@Composable
private fun TarjetaLote(lote: Lote, alSeleccionar: () -> Unit) {
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
            Text("Toca para ver labores", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun TarjetaLabor(labor: Labor) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("${labor.tipo.name} · ${labor.fecha}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Text("Responsable: ${labor.responsable}")
            Text("Cantidad: ${labor.cantidad}")
            if (labor.observacion.isNotBlank()) {
                Text("Observación: ${labor.observacion}")
            }
            if (labor.pendiente) {
                Text("Pendiente de sincronizar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun NuevaLaborDialog(
    alCerrar: () -> Unit,
    alGuardar: (TipoLabor, String, String, String) -> String?
) {
    var tipo by remember { mutableStateOf(TipoLabor.RIEGO) }
    var fecha by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
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
                    error = alGuardar(tipo, fecha, cantidad, observacion)
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
