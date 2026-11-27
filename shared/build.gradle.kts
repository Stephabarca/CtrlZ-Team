import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    android {
       namespace = "com.example.videojuego.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")
        }
        jvmMain.dependencies {
            // Decodifica el mp3 de la música en escritorio (la JVM solo trae wav).
            implementation("javazoom:jlayer:1.0.1")
            // Reproduce el video de inicio en escritorio (la JVM no trae video). Va con los archivos nativos
            // del sistema donde se compila.
            val fxPlatform = run {
                val os = System.getProperty("os.name").lowercase()
                val arm = System.getProperty("os.arch") == "aarch64"
                when {
                    os.contains("win") -> "win"
                    os.contains("mac") -> if (arm) "mac-aarch64" else "mac"
                    else -> if (arm) "linux-aarch64" else "linux"
                }
            }
            for (module in listOf("base", "graphics", "media", "swing")) {
                implementation("org.openjfx:javafx-$module:21.0.5:$fxPlatform")
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}