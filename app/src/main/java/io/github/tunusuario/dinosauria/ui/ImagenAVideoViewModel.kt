package io.github.tunusuario.dinosauria.ui // <-- CAMBIA "tunusuario" POR TU USUARIO

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class ImagenAVideoViewModel : ViewModel() {

    private val _estado = MutableStateFlow<EstadoVideo>(EstadoVideo.Inactivo)
    val estado: StateFlow<EstadoVideo> = _estado

    // ⚠️ PEGA AQUÍ TU MISMO TOKEN DE HUGGING FACE
    private val apiToken = "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"

    // Modelo de Hugging Face para convertir imagen a video
    private val apiUrl = "https://api-inference.huggingface.co/models/stabilityai/stable-video-diffusion-img2vid-xt"

    fun generarVideoDesdeImagen(imagenBytes: ByteArray) {
        viewModelScope.launch {
            _estado.value = EstadoVideo.Cargando
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS) // Tiempo de espera largo para video
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                // Enviamos la imagen directamente en el cuerpo de la petición
                val body = imagenBytes.toRequestBody("image/jpeg".toMediaType())
                
                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer $apiToken")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _estado.value = EstadoVideo.Error("Error ${response.code}: El modelo puede estar ocupado. Intenta de nuevo en un minuto.")
                    return@launch
                }

                val responseBody = response.body?.bytes()
                if (responseBody != null) {
                    // Convertimos el video a Base64 para que el reproductor de la app lo pueda leer
                    val base64Video = Base64.encodeToString(responseBody, Base64.NO_WRAP)
                    _estado.value = EstadoVideo.Exito("data:video/mp4;base64,$base64Video")
                } else {
                    _estado.value = EstadoVideo.Error("Respuesta vacía de la IA.")
                }

            } catch (e: Exception) {
                _estado.value = EstadoVideo.Error("Error de red: ${e.message}")
            }
        }
    }
}
