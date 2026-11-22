package io.github.tunusuario.dinosauria.ui // <-- Cambia "tunusuario" por tu usuario

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TextoAVideoViewModel : ViewModel() {
    private val _estado = MutableStateFlow<EstadoVideo>(EstadoVideo.Inactivo)
    val estado: StateFlow<EstadoVideo> = _estado

    // Sustituye este marcador por tu token de Hugging Face (no publiques tokens reales).
    private val apiToken = "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"
    private val apiUrl = "https://api-inference.huggingface.co/models/damo-vilab/text-to-video-ms-1.7b"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun generarVideo(prompt: String) {
        val texto = prompt.trim()
        if (texto.isEmpty()) {
            _estado.value = EstadoVideo.Error("Escribe una descripción para generar el video.")
            return
        }

        viewModelScope.launch {
            _estado.value = EstadoVideo.Cargando
            try {
                val dataUrl = withContext(Dispatchers.IO) {
                    val json = JSONObject().put("inputs", texto)
                    val body = json.toString().toRequestBody("application/json".toMediaType())
                    val request = Request.Builder()
                        .url(apiUrl)
                        .header("Authorization", "Bearer $apiToken")
                        .post(body)
                        .build()

                    client.newCall(request).execute().use { response ->
                        val bytes = response.body?.bytes()
                        if (!response.isSuccessful) {
                            val detail = bytes?.toString(Charsets.UTF_8)?.take(500)
                            throw IllegalStateException("HTTP ${response.code}: ${detail?.takeIf { it.isNotBlank() } ?: response.message}")
                        }
                        if (bytes.isNullOrEmpty()) throw IllegalStateException("La IA devolvió un video vacío.")
                        val contentType = response.header("Content-Type").orEmpty()
                        if (contentType.contains("json", ignoreCase = true) || contentType.startsWith("text/", ignoreCase = true)) {
                            throw IllegalStateException("La API devolvió texto en lugar de un video: ${bytes.toString(Charsets.UTF_8).take(300)}")
                        }
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        "data:video/mp4;base64,$base64"
                    }
                }
                _estado.value = EstadoVideo.Exito(dataUrl)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = EstadoVideo.Error(e.message ?: "No se pudo generar el video.")
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
