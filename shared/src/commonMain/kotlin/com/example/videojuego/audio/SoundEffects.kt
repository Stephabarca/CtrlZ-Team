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

    /** Pesca (Noche 1): la caña recoge la línea cuando el pez pica. */
    REEL("files/sounds/reel_sound.wav"),

    /** Pesca (Noche 1): se atrapó el pez. */
    CATCH("files/sounds/catch_sound.wav"),

    /**
     * Minijuegos: se perdió el pez por no jalar a tiempo o por jalar antes (Noche 1), se falló el orden de los
     * objetos (Noche 2) o la balsa chocó con un obstáculo (Noche 3).
     */
    FAIL("files/sounds/fail_sound.wav"),

    /** Fogata (Noche 2): se acertó la combinación completa. */
    SUCCESS("files/sounds/success_sound.wav"),

    /** Hydro Hopper (Noche 3): la balsa salta. */
    JUMP("files/sounds/jump_sound.wav"),
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
