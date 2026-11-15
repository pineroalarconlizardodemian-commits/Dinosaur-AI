package io.github.tunusuario.dinosauria // <-- CAMBIA "tunusuario" POR TU USUARIO

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Importamos nuestras pantallas
import io.github.tunusuario.dinosauria.ui.PantallaPrincipal
import io.github.tunusuario.dinosauria.ui.TextoAImagen
import io.github.tunusuario.dinosauria.ui.ImagenAVideo
import io.github.tunusuario.dinosauria.ui.TextoAVideo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "inicio") {
                        
                        // 1. Pantalla Principal (Los 3 botones)
                        composable("inicio") {
                            PantallaPrincipal(
                                onTextoAVideo = { navController.navigate("texto_video") },
                                onImagenAVideo = { navController.navigate("imagen_video") },
                                onTextoAImagen = { navController.navigate("texto_imagen") }
                            )
                        }

                        // 2. Pantalla: Texto a Video
                        composable("texto_video") {
                            TextoAVideo(onVolver = { navController.popBackStack() })
                        }

                        // 3. Pantalla: Imagen a Video
                        composable("imagen_video") {
                            ImagenAVideo(onVolver = { navController.popBackStack() })
                        }

                        // 4. Pantalla: Texto a Imagen
                        composable("texto_imagen") {
                            TextoAImagen(onVolver = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
