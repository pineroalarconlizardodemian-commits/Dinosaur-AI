package io.github.tunusuario.dinosauria.ui // <-- CAMBIA "tunusuario" POR TU USUARIO

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

class TextoAImagenViewModel : ViewModel() {
    private val _estado = MutableStateFlow<EstadoGeneracion>(EstadoGeneracion.Inactivo)
    val estado: StateFlow<EstadoGeneracion> = _estado

    // Sustituye este marcador por tu token de Hugging Face (no publiques tokens reales).
    private val apiToken = "REEMPLAZA_CON_TU_TOKEN_HF"
    private val apiUrl = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun generarImagen(prompt: String) {
        val texto = prompt.trim()
        if (texto.isEmpty()) {
            _estado.value = EstadoGeneracion.Error("Escribe una descripción para generar la imagen.")
            return
        }

        viewModelScope.launch {
            _estado.value = EstadoGeneracion.Cargando
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
                        if (bytes.isNullOrEmpty()) throw IllegalStateException("La IA devolvió una respuesta vacía.")
                        val contentType = response.header("Content-Type").orEmpty()
                        if (contentType.contains("json", ignoreCase = true) || contentType.startsWith("text/", ignoreCase = true)) {
                            throw IllegalStateException("La API devolvió texto en lugar de una imagen: ${bytes.toString(Charsets.UTF_8).take(300)}")
                        }
                        val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        val mime = contentType.substringBefore(';').takeIf { it.startsWith("image/") } ?: "image/png"
                        "data:$mime;base64,$base64"
                    }
                }
                _estado.value = EstadoGeneracion.Exito(dataUrl)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _estado.value = EstadoGeneracion.Error(e.message ?: "No se pudo generar la imagen.")
            }
        }
    }
}


sealed class EstadoGeneracion {
    object Inactivo : EstadoGeneracion()
    object Cargando : EstadoGeneracion()
    data class Exito(val url: String) : EstadoGeneracion()
    data class Error(val mensaje: String) : EstadoGeneracion()
}
