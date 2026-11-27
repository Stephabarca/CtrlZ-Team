package com.example.videojuego.audio

import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual fun createSfxPlayer(): SfxPlayer = DesktopSfxPlayer()

/** Escritorio: cada sonido es un Clip de Java Sound que se cierra solo al terminar. */
private class DesktopSfxPlayer : SfxPlayer {
    private val decoded = HashMap<ByteArray, Pcm>()

    override fun play(bytes: ByteArray, volume: Float) {
        val pcm = synchronized(decoded) { decoded.getOrPut(bytes) { decodeToPcm(bytes) ?: return } }
        val data = ByteArray(pcm.data.size)
        scaleInto(pcm.data, 0, pcm.data.size, data, volume.coerceIn(0f, 1f))
        val clip = AudioSystem.getClip()
        clip.addLineListener { if (it.type == LineEvent.Type.STOP) clip.close() }
        clip.open(pcm.format, data, 0, data.size)
        clip.start()
    }
}
