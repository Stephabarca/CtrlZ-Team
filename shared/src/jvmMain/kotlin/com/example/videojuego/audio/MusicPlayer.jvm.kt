package com.example.videojuego.audio

import javazoom.jl.decoder.Bitstream
import javazoom.jl.decoder.Decoder
import javazoom.jl.decoder.Header
import javazoom.jl.decoder.SampleBuffer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine

actual fun createMusicPlayer(): MusicPlayer = DesktopMusicPlayer()

/** Audio ya decodificado: muestras de 16 bits, little-endian, intercaladas por canal. */
internal class Pcm(val data: ByteArray, val format: AudioFormat)

/**
 * Convierte un archivo (wav o mp3) a PCM de 16 bits. Los wav los lee Java Sound; los mp3 los decodifica JLayer.
 * Devuelve null si no se puede leer.
 */
internal fun decodeToPcm(bytes: ByteArray): Pcm? = try {
    if (isWav(bytes)) decodeWav(bytes) else decodeMp3(bytes)
} catch (e: Exception) {
    null
}

private fun isWav(bytes: ByteArray): Boolean =
    bytes.size > 12 && bytes[0] == 'R'.code.toByte() && bytes[1] == 'I'.code.toByte() &&
        bytes[2] == 'F'.code.toByte() && bytes[3] == 'F'.code.toByte()

private fun decodeWav(bytes: ByteArray): Pcm {
    val source = AudioSystem.getAudioInputStream(ByteArrayInputStream(bytes))
    val f = source.format
    val target = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, f.sampleRate, 16, f.channels, f.channels * 2, f.sampleRate, false)
    val pcm = if (f.matches(target)) source else AudioSystem.getAudioInputStream(target, source)
    return Pcm(pcm.readAllBytes(), target)
}

private fun decodeMp3(bytes: ByteArray): Pcm? {
    val bitstream = Bitstream(ByteArrayInputStream(bytes))
    val decoder = Decoder()
    val out = ByteArrayOutputStream()
    var sampleRate = 0
    var channels = 0
    try {
        while (true) {
            val header: Header = bitstream.readFrame() ?: break
            val buffer = decoder.decodeFrame(header, bitstream) as SampleBuffer
            sampleRate = buffer.sampleFrequency
            channels = buffer.channelCount
            val samples = buffer.buffer
            for (i in 0 until buffer.bufferLength) {
                val s = samples[i].toInt()
                out.write(s and 0xFF)
                out.write((s shr 8) and 0xFF)
            }
            bitstream.closeFrame()
        }
    } finally {
        bitstream.close()
    }
    if (sampleRate == 0 || channels == 0) return null
    return Pcm(out.toByteArray(), AudioFormat(sampleRate.toFloat(), 16, channels, true, false))
}

/** Copia [src] en [dst] multiplicando cada muestra de 16 bits por [volume]. */
internal fun scaleInto(src: ByteArray, srcOffset: Int, length: Int, dst: ByteArray, volume: Float) {
    var i = 0
    while (i + 1 < length) {
        val lo = src[srcOffset + i].toInt() and 0xFF
        val hi = src[srcOffset + i + 1].toInt()
        val scaled = (((hi shl 8) or lo) * volume).toInt().coerceIn(-32768, 32767)
        dst[i] = (scaled and 0xFF).toByte()
        dst[i + 1] = ((scaled shr 8) and 0xFF).toByte()
        i += 2
    }
}

/**
 * Escritorio: decodifica el archivo completo a PCM y lo envía a la tarjeta de sonido desde un hilo propio.
 * El volumen se aplica a cada muestra, así funciona igual en cualquier equipo.
 */
private class DesktopMusicPlayer : MusicPlayer {
    @Volatile private var volume = 1f
    @Volatile private var paused = false
    private var pausedByUs = false
    private var worker: Thread? = null
    private var line: SourceDataLine? = null
    private val lock = Object()

    override fun play(bytes: ByteArray, volume: Float, loop: Boolean) {
        stop()
        this.volume = volume.coerceIn(0f, 1f)
        val thread = Thread({ run(bytes, loop) }, "musica-fondo")
        thread.isDaemon = true
        worker = thread
        thread.start()
    }

    private fun run(bytes: ByteArray, loop: Boolean) {
        val me = Thread.currentThread()
        val pcm = decodeToPcm(bytes) ?: return
        val out = try {
            (AudioSystem.getLine(DataLine.Info(SourceDataLine::class.java, pcm.format)) as SourceDataLine).also {
                it.open(pcm.format)
                it.start()
            }
        } catch (e: Exception) {
            return
        }
        synchronized(lock) {
            if (worker !== me) {
                out.close()
                return
            }
            line = out
        }
        val frame = pcm.format.frameSize
        val chunk = 4096 / frame * frame
        val scratch = ByteArray(chunk)
        try {
            do {
                var pos = 0
                while (pos < pcm.data.size && worker === me) {
                    if (paused) {
                        Thread.sleep(50)
                        continue
                    }
                    val n = minOf(chunk, pcm.data.size - pos)
                    scaleInto(pcm.data, pos, n, scratch, volume)
                    out.write(scratch, 0, n)
                    pos += n
                }
            } while (loop && worker === me)
            if (worker === me) out.drain()
        } catch (e: InterruptedException) {
            // stop() pidió terminar
        } finally {
            out.stop()
            out.close()
            synchronized(lock) { if (line === out) line = null }
        }
    }

    override fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
    }

    override fun pause() {
        if (worker != null && !paused) {
            paused = true
            pausedByUs = true
            line?.stop()
        }
    }

    override fun resume() {
        if (pausedByUs) {
            pausedByUs = false
            line?.start()
            paused = false
        }
    }

    override fun stop() {
        val old: Thread?
        synchronized(lock) {
            old = worker
            worker = null
            // Cerrar la línea corta el sonido al instante y libera al hilo si estaba esperando en write().
            line?.close()
            line = null
        }
        old?.interrupt()
        paused = false
        pausedByUs = false
    }
}
