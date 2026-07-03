plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.scantickets.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.scantickets.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "4.0"
    }

    // La clé de signature n'est PAS versionnée (dépôt public). Si
    // signing/scantickets.jks est présent localement, la release est signée
    // avec ; sinon (CI notamment) on retombe sur la signature de debug —
    // l'APK reste installable, seule la continuité des mises à jour change.
    val keystoreRelease = rootProject.file("signing/scantickets.jks")
    signingConfigs {
        create("release") {
            storeFile = keystoreRelease
            storePassword = System.getenv("SCANTICKETS_KEYSTORE_PASSWORD") ?: "scantickets2026"
            keyAlias = "scantickets"
            keyPassword = System.getenv("SCANTICKETS_KEYSTORE_PASSWORD") ?: "scantickets2026"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = if (keystoreRelease.exists()) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Scan de documents (recadrage/redressement automatique, hors ligne)
    implementation("com.google.android.gms:play-services-mlkit-document-scanner:16.0.0-beta1")

    // OCR local (modèle embarqué dans l'APK, 100 % hors ligne)
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")

    // Accès au dossier de sortie choisi par l'utilisateur (SAF)
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Miniatures des tickets
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Tests unitaires (parseur de tickets)
    testImplementation("junit:junit:4.13.2")
}
