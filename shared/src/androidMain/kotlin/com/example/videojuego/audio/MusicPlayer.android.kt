package com.example.videojuego.audio

import android.media.MediaDataSource
import android.media.MediaPlayer

actual fun createMusicPlayer(): MusicPlayer = AndroidMusicPlayer()

/** MediaPlayer de Android leyendo el mp3 desde memoria (no necesita Context ni archivos temporales). */
private class AndroidMusicPlayer : MusicPlayer {
    private var player: MediaPlayer? = null
    private var volume = 1f
    private var pausedByUs = false

    override fun play(bytes: ByteArray, volume: Float, loop: Boolean) {
        stop()
        this.volume = volume.coerceIn(0f, 1f)
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
            p.isLooping = loop
            p.setVolume(this.volume, this.volume)
            p.setOnPreparedListener { it.start() }
            p.prepareAsync()
            player = p
        } catch (e: Exception) {
            p.release()
            player = null
        }
    }

    override fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        player?.setVolume(this.volume, this.volume)
    }

    override fun pause() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
            pausedByUs = true
        }
    }

    override fun resume() {
        val p = player ?: return
        if (pausedByUs) {
            p.start()
            pausedByUs = false
        }
    }

    override fun stop() {
        player?.let {
            try {
                it.stop()
            } catch (e: IllegalStateException) {
                // todavía no estaba lista; basta con liberarla
            }
            it.release()
        }
        player = null
        pausedByUs = false
    }
}
