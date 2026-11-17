package com.example.videojuego.audio

import android.media.MediaDataSource
import android.media.MediaPlayer

actual fun createSfxPlayer(): SfxPlayer = AndroidSfxPlayer()

/** Android: un MediaPlayer por sonido, leyendo el wav desde memoria; se libera al terminar. */
private class AndroidSfxPlayer : SfxPlayer {
    // Referencias a los que están sonando: si no, el recolector de basura puede cortarlos a la mitad.
    private val playing = mutableSetOf<MediaPlayer>()

    override fun play(bytes: ByteArray, volume: Float) {
        val v = volume.coerceIn(0f, 1f)
        val p = MediaPlayer()
        try {
            p.setDataSource(object : MediaDataSource() {
                override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
                    if (position >= bytes.size) return -1
                    val n = minOf(size, (bytes.size - position).toInt())
                    System.arraycopy(bytes, position.toInt(), buffer, offset, n)
                    return n
                }

                override fun getSize(): Long = bytes.size.toLong()

                override fun close() {}
            })
            p.setVolume(v, v)
            p.setOnPreparedListener { it.start() }
            p.setOnCompletionListener { done(it) }
            p.setOnErrorListener { mp, _, _ -> done(mp); true }
            synchronized(playing) { playing += p }
            p.prepareAsync()
        } catch (e: Exception) {
            done(p)
        }
    }

    private fun done(p: MediaPlayer) {
        synchronized(playing) { playing -= p }
        p.release()
    }
}
