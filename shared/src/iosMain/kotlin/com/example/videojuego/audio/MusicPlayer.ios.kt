package com.example.videojuego.audio

actual fun createMusicPlayer(): MusicPlayer = SilentMusicPlayer()

/** iOS: por ahora sin música (pendiente AVAudioPlayer). */
private class SilentMusicPlayer : MusicPlayer {
    override fun play(bytes: ByteArray, volume: Float, loop: Boolean) {}
    override fun setVolume(volume: Float) {}
    override fun pause() {}
    override fun resume() {}
    override fun stop() {}
}
