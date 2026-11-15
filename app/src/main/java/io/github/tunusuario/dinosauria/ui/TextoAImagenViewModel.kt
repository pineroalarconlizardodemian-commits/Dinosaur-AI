package io.github.tunusuario.dinosauria.ui // <-- CAMBIA "tunusuario" POR TU USUARIO

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TextoAImagenViewModel : ViewModel() {

    private val _estado = MutableStateFlow<EstadoGeneracion>(EstadoGeneracion.Inactivo)
    val estado: StateFlow<EstadoGeneracion> = _estado

    // ==========================================================
    // ⚠️ PEGA AQUÍ TU TOKEN DE HUGGING FACE (hf_...)
    // ==========================================================
    private val apiToken = "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"

    // URL del modelo (Stable Diffusion XL)
    private val apiUrl = "https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0"

    fun generarImagen(prompt: String) {
        if (prompt.isBlank()) return
        
        viewModelScope.launch {
            _estado.value = EstadoGeneracion.Cargando
            try {
                // Configuramos un tiempo de espera largo (la IA a veces tarda en "despertar")
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
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
                    _estado.value = EstadoGeneracion.Error("Error ${response.code}: Revisa tu token o intenta de nuevo.")
                    return@launch
                }

                val responseBody = response.body?.bytes()
                if (responseBody != null) {
                    // Convertimos la imagen a Base64 para mostrarla directamente
                    val base64Image = android.util.Base64.encodeToString(responseBody, android.util.Base64.DEFAULT)
                    val dataUrl = "data:image/jpeg;base64,$base64Image"
                    _estado.value = EstadoGeneracion.Exito(dataUrl)
                } else {
                    _estado.value = EstadoGeneracion.Error("Respuesta vacía de la IA.")
                }

            } catch (e: Exception) {
                _estado.value = EstadoGeneracion.Error("Error de red: ${e.message}")
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
