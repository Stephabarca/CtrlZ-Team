package com.example.videojuego.data

import kotlinx.serialization.json.Json

/** Configuración única de JSON para contenido y guardado. */
val GameJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
