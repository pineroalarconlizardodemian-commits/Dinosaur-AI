package io.github.tunusuario.dinosauria.ui // <-- CAMBIA "tunusuario" POR TU USUARIO

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImagenAVideo(
    onVolver: () -> Unit, 
    viewModel: ImagenAVideoViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Lanzador para abrir la galería (Photo Picker de Android)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Leemos la imagen seleccionada y la enviamos al ViewModel
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                if (bytes != null) {
                    viewModel.generarVideoDesdeImagen(bytes)
                }
            } catch (e: Exception) {
                // Si falla la lectura de la imagen, no hacemos nada (o podrías mostrar un error)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Button(onClick = onVolver) { Text("<- Volver") }
        }
        
        Spacer(Modifier.height(16.dp))
        Text("Imagen a Video", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        
        Button(
            onClick = { 
                launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
            },
            enabled = estado !is EstadoVideo.Cargando,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Seleccionar Imagen de la Galería")
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            when (val e = estado) {
                is EstadoVideo.Inactivo -> {
                    Text("Elige una foto para animarla", color = MaterialTheme.colorScheme.outline)
                }
                is EstadoVideo.Cargando -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Animando tu imagen...\n(Esto puede tardar varios minutos)")
                    }
                }
                is EstadoVideo.Exito -> {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.parse(e.url))
                                setMediaController(MediaController(ctx).also { it.setAnchorView(this) })
                                setOnPreparedListener { it.isLooping = true; start() }
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
