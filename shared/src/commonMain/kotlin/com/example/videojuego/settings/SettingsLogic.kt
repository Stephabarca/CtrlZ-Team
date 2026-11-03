package com.example.videojuego.settings

import com.example.videojuego.data.save.GameSettings
import kotlin.math.roundToLong

/**
 * Conversión entre lo que ve el jugador (una velocidad de 0 a 1) y lo que guarda el juego
 * (milisegundos de espera entre letras: menos espera = texto más rápido).
 */
object SettingsLogic {
    const val SLOWEST_DELAY_MS = 100L
    const val FASTEST_DELAY_MS = 10L

    /** 0 = texto más lento, 1 = texto más rápido. */
    fun speedFromDelay(delayMs: Long): Float {
        val clamped = delayMs.coerceIn(FASTEST_DELAY_MS, SLOWEST_DELAY_MS)
        return (SLOWEST_DELAY_MS - clamped).toFloat() / (SLOWEST_DELAY_MS - FASTEST_DELAY_MS)
    }

    fun delayFromSpeed(speed: Float): Long =
        (SLOWEST_DELAY_MS - (SLOWEST_DELAY_MS - FASTEST_DELAY_MS) * speed.coerceIn(0f, 1f)).roundToLong()

    fun withTextSpeed(settings: GameSettings, speed: Float) =
        settings.copy(textDelayMs = delayFromSpeed(speed)).sanitized()

    fun withMusic(settings: GameSettings, volume: Float) = settings.copy(musicVolume = volume).sanitized()

    fun withSfx(settings: GameSettings, volume: Float) = settings.copy(sfxVolume = volume).sanitized()

    fun percent(value: Float): Int = (value.coerceIn(0f, 1f) * 100f).roundToLong().toInt()
}
