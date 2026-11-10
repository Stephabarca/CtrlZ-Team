package com.example.videojuego.audio

/** Reproductor de música de fondo. Cada plataforma tiene su propia implementación ([createMusicPlayer]). */
interface MusicPlayer {
    /** Empieza [bytes] (un archivo mp3) desde el principio; reemplaza lo que sonara. */
    fun play(bytes: ByteArray, volume: Float, loop: Boolean = true)

    /** Cambia el volumen (0 a 1) sin reiniciar la canción. */
    fun setVolume(volume: Float)

    /** Pausa (por ejemplo al salir de la app). */
    fun pause()

    /** Sigue después de [pause]. No hace nada si no estaba pausada por [pause]. */
    fun resume()

    fun stop()
}

expect fun createMusicPlayer(): MusicPlayer
