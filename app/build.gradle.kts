import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) load(file.inputStream())
}

android {
    namespace = "com.francekim.empreintedelame"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.francekim.empreintedelame"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "1.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile     = file(keystoreProperties.getProperty("storeFile", "empreinte.jks"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias      = keystoreProperties.getProperty("keyAlias")
            keyPassword   = keystoreProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            // R8 : exigé par Play au-delà de 10 Mo de DEX (échéance février 2027)
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sans keystore.properties (compilation F-Droid), l'APK sort non signé
            signingConfig = if (keystoreProperties.isEmpty()) null
                            else signingConfigs.getByName("release")
            // Build reproductible F-Droid : ne pas inscrire le commit git dans l'APK
            vcsInfo {
                include = false
            }
        }
        // Copie du release (R8 compris), signée avec la clé debug : s'installe
        // à côté de la version Play pour tester sans effacer les données
        create("r8test") {
            initWith(getByName("release"))
            applicationIdSuffix = ".r8test"
            versionNameSuffix = "-r8test"
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        // java.time (LocalDate, DateTimeFormatter…) n'existe qu'à partir d'Android 8 :
        // sans ça, tout ce qui touche aux dates plante sur les appareils Android 7.
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    bundle {
        // Sans ça, Play ne livre que le split de la langue du téléphone : le sélecteur
        // de langue in-app n'a alors aucun strings.xml de l'autre langue à charger.
        language {
            enableSplit = false
        }
    }
    dependenciesInfo {
        // Bloc chiffré de Google dans la signature de l'APK : F-Droid le refuse
        includeInApk = false
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.datastore.preferences)
    ksp(libs.androidx.room.compiler)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}