package io.github.tunusuario.dinosauria.ui // <-- CAMBIA "tunusuario" POR TU USUARIO

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TextoAVideo(
    onVolver: () -> Unit, 
    viewModel: TextoAVideoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var prompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = onVolver) { Text("<- Volver") }
        }
        
        Spacer(Modifier.height(16.dp))
        Text("Texto a Video", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Describe el video...") },
            modifier = Modifier.fillMaxWidth(),
            enabled = estado !is EstadoVideo.Cargando
        )
        
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.generarVideo(prompt) },
            enabled = estado !is EstadoVideo.Cargando && prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Generar Video")
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val e = estado) {
                is EstadoVideo.Inactivo -> {
                    Text("El video aparecerá aquí", color = MaterialTheme.colorScheme.outline)
                }
                is EstadoVideo.Cargando -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Generando video...\n(Esto puede tardar varios minutos)")
                    }
                }
                is EstadoVideo.Exito -> {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                setVideoURI(Uri.parse(e.url))
                                setMediaController(MediaController(context).also { it.setAnchorView(this) })
                                setOnPreparedListener { it.isLooping = true }
                                start()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is EstadoVideo.Error -> {
                    Text(e.mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
