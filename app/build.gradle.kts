plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "io.github.tunusuario.dinosauria" // <-- AQUÍ PON TU USUARIO REAL
    compileSdk = 35

    defaultConfig {
        applicationId = "io.github.tunusuario.dinosauria" // <-- AQUÍ TAMBIÉN
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        ndk {
            abiFilters += "arm64-v8a"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.03"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.1")
    
    // --- LIBRERÍAS PARA LA IA ---
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("io.github.enyason:replicate-kotlin:0.1.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    // ----------------------------

    debugImplementation("androidx.compose.ui:ui-tooling")
}
