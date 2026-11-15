package io.github.tunusuario.dinosauria.ui // <-- CAMBIA "tunusuario"

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun TextoAImagen(
    onVolver: () -> Unit, 
    viewModel: TextoAImagenViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Botón para volver
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = onVolver) { Text("<- Volver") }
        }
        
        Spacer(Modifier.height(16.dp))
        Text("Texto a Imagen", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Ej: Un dinosaurio fotorrealista en la luna") },
            modifier = Modifier.fillMaxWidth(),
            enabled = estado !is EstadoGeneracion.Cargando
        )
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.generarImagen(prompt) },
            enabled = estado !is EstadoGeneracion.Cargando && prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Generar Imagen")
        }

        Spacer(Modifier.height(24.dp))

        // Área de resultados
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val e = estado) {
                is EstadoGeneracion.Inactivo -> {
                    Text("Escribe un prompt y pulsa Generar", color = MaterialTheme.colorScheme.outline)
                }
                is EstadoGeneracion.Cargando -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Generando imagen...\n(La primera vez puede tardar hasta 1 minuto)")
                    }
                }
                is EstadoGeneracion.Exito -> {
                    AsyncImage(
                        model = e.url,
                        contentDescription = "Imagen generada",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is EstadoGeneracion.Error -> {
                    Text(
                        text = e.mensaje, 
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
