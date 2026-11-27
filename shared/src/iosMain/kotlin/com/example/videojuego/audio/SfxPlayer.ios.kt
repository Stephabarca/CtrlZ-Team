package com.example.videojuego.audio

actual fun createSfxPlayer(): SfxPlayer = SilentSfxPlayer()

/** iOS: por ahora sin efectos (pendiente AVAudioPlayer). */
private class SilentSfxPlayer : SfxPlayer {
    override fun play(bytes: ByteArray, volume: Float) {}
}
