package pe.edu.upeu.agrocampo_llontop_vivas.presentation.lotes

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import org.koin.compose.viewmodel.koinViewModel
import pe.edu.upeu.agrocampo_llontop_vivas.domain.model.Lote
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
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
            Text(
                "Sin conexión: $error",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (estado.lotes.isEmpty() && !estado.sincronizando) {
            Text(
                "No hay lotes guardados. Inicia sesión con el servidor activo y pulsa Actualizar.",
                modifier = Modifier.padding(top = 24.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(estado.lotes, key = { it.id }) { lote ->
                TarjetaLote(lote)
            }
        }
    }
}

@Composable
private fun TarjetaLote(lote: Lote) {
    Card(modifier = Modifier.fillMaxWidth()) {
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
        }
    }
}
