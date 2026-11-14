package io.github.tunusuario.dinosauria.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PantallaPrincipal(
    onTextoAVideo: () -> Unit = {},
    onImagenAVideo: () -> Unit = {},
    onTextoAImagen: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🦖 DinosaurAI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))

        Button(
            onClick = onTextoAVideo,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Texto a Video", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onImagenAVideo,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Imagen a Video", fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onTextoAImagen,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Texto a Imagen", fontSize = 18.sp)
        }
    }
}
