package com.example.videojuego.ui.splash

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

/** Android: VideoView centrado (mantiene la proporción) leyendo una copia del video en la caché de la app. */
@SuppressLint("ClickableViewAccessibility")
@Composable
actual fun VideoPlayer(bytes: ByteArray, volume: Float, onFinished: () -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val finish by rememberUpdatedState(onFinished)
    val file = remember(bytes) {
        try {
            File(context.cacheDir, "control_z_intro.mp4").apply { writeBytes(bytes) }
        } catch (e: Exception) {
            null
        }
    }
    if (file == null) {
        LaunchedEffect(Unit) { finish() }
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val video = VideoView(ctx).apply {
                setVideoPath(file.absolutePath)
                setOnPreparedListener { mp ->
                    val v = volume.coerceIn(0f, 1f)
                    mp.setVolume(v, v)
                    start()
                }
                setOnCompletionListener { finish() }
                setOnErrorListener { _, _, _ ->
                    finish()
                    true
                }
            }
            FrameLayout(ctx).apply {
                setBackgroundColor(Color.BLACK)
                addView(
                    video,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER,
                    ),
                )
                // Tocar en cualquier parte salta la animación.
                setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) finish()
                    true
                }
                video.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) finish()
                    true
                }
            }
        },
        onRelease = { layout ->
            (layout.getChildAt(0) as? VideoView)?.stopPlayback()
            file.delete()
        },
    )
}
