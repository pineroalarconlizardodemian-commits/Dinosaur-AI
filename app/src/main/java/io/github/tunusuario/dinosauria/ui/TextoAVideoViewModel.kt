package io.github.tunusuario.dinosauria.ui // <-- Cambia "tunusuario"

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import android.util.Base64

class TextoAVideoViewModel : ViewModel() {

    private val _estado = MutableStateFlow<EstadoVideo>(EstadoVideo.Inactivo)
    val estado: StateFlow<EstadoVideo> = _estado

    // ⚠️ PEGA AQUÍ TU MISMO TOKEN DE HUGGING FACE
    private val apiToken = "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"

    // Usamos un modelo de video de Hugging Face
    private val apiUrl = "https://api-inference.huggingface.co/models/damo-vilab/text-to-video-ms-1.7b"

    fun generarVideo(prompt: String) {
        if (prompt.isBlank()) return
        
        viewModelScope.launch {
            _estado.value = EstadoVideo.Cargando
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(120, TimeUnit.SECONDS) // Los videos tardan más
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val json = JSONObject().apply {
                    put("inputs", prompt)
                }

                val body = json.toString().toRequestBody("application/json".toMediaType())
                
                val request = Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer $apiToken")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _estado.value = EstadoVideo.Error("Error ${response.code}: El modelo puede estar dormido o saturado. Intenta de nuevo.")
                    return@launch
                }

                val responseBody = response.body?.bytes()
                if (responseBody != null) {
                    // Convertimos el video a Base64 para reproducirlo
                    val base64Video = Base64.encodeToString(responseBody, Base64.NO_WRAP)
                    val dataUrl = "data:video/mp4;base64,$base64Video"
                    _estado.value = EstadoVideo.Exito(dataUrl)
                } else {
                    _estado.value = EstadoVideo.Error("Respuesta vacía de la IA.")
                }

            } catch (e: Exception) {
                _estado.value = EstadoVideo.Error("Error de red: ${e.message}")
            }
        }
    }
}

sealed class EstadoVideo {
    object Inactivo : EstadoVideo()
    object Cargando : EstadoVideo()
    data class Exito(val url: String) : EstadoVideo()
    data class Error(val mensaje: String) : EstadoVideo()
}
