package com.example.videojuego.audio

import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Comprueba que en escritorio se pueden decodificar la música (mp3) y todos los sonidos (wav). */
class DesktopAudioDecodeTest {
    private fun resource(path: String): ByteArray = File("src/commonMain/composeResources/$path").readBytes()

    @Test
    fun everyTrackAndEffect_decodesToPcm() {
        val paths = MusicTrack.entries.map { it.path } + SoundEffect.entries.map { it.path }
        for (path in paths) {
            val pcm = assertNotNull(decodeToPcm(resource(path)), path)
            assertTrue(pcm.data.isNotEmpty(), path)
            assertTrue(pcm.format.sampleSizeInBits == 16, path)
        }
    }
}
