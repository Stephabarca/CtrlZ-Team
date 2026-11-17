package com.example.videojuego.audio

import videojuego.shared.generated.resources.Res

/** Sonidos cortos del juego y su archivo (dentro de composeResources). */
enum class SoundEffect(val path: String) {
    /** Golpe de madera al tocar cualquier botón. */
    CLICK("files/sounds/wood_sound.wav"),

    /** La respuesta subió la afinidad. */
    POSITIVE("files/sounds/positive_sound.wav"),

    /** La respuesta bajó la afinidad. */
    NEGATIVE("files/sounds/negative_sound.wav"),
}

/** Reproduce un sonido corto encima de lo que suene. Cada plataforma tiene su implementación. */
interface SfxPlayer {
    fun play(bytes: ByteArray, volume: Float)
}

expect fun createSfxPlayer(): SfxPlayer

/**
 * Punto único para los efectos de sonido. [preload] (lo llama App al abrir el juego) lee los archivos una vez;
 * después [play] suena al instante desde cualquier parte. [volume] es el volumen de efectos de Ajustes.
 */
object Sfx {
    var volume: Float = 0.8f
    private val player: SfxPlayer by lazy { createSfxPlayer() }
    private val cache = mutableMapOf<SoundEffect, ByteArray>()

    suspend fun preload() {
        for (effect in SoundEffect.entries) {
            if (effect in cache) continue
            try {
                cache[effect] = Res.readBytes(effect.path)
            } catch (e: Exception) {
                // sin ese archivo el juego sigue, solo que sin ese sonido
            }
        }
    }

    fun play(effect: SoundEffect) {
        val bytes = cache[effect] ?: return
        if (volume <= 0f) return
        try {
            player.play(bytes, volume)
        } catch (e: Exception) {
            // un sonido que falla nunca debe romper el juego
        }
    }
}
