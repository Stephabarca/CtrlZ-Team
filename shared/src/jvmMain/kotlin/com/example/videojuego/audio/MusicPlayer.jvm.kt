package com.example.videojuego.audio

actual fun createMusicPlayer(): MusicPlayer = SilentMusicPlayer()

/**
 * Escritorio: por ahora sin música (la JVM no decodifica mp3 sin una librería extra, por ejemplo JLayer).
 * La música se oye en Android, que es donde se presenta el juego.
 */
private class SilentMusicPlayer : MusicPlayer {
    override fun play(bytes: ByteArray, volume: Float, loop: Boolean) {}
    override fun setVolume(volume: Float) {}
    override fun pause() {}
    override fun resume() {}
    override fun stop() {}
}
