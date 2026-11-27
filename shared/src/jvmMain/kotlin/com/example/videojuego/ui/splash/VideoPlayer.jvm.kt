package com.example.videojuego.ui.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import java.io.File
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.scene.media.MediaView
import javax.swing.SwingUtilities

/**
 * Escritorio: la JVM no trae video, así que se usa JavaFX Media (lee H.264 + AAC) dentro de un JFXPanel.
 * El video se copia a un archivo temporal porque JavaFX solo reproduce desde una URL.
 * Clic o cualquier tecla sobre el video lo salta.
 */
@Composable
actual fun VideoPlayer(bytes: ByteArray, volume: Float, onFinished: () -> Unit, modifier: Modifier) {
    val current by rememberUpdatedState(onFinished)
    // Los avisos de JavaFX llegan en su propio hilo; el estado de Compose se cambia en el hilo de Swing.
    val finish: () -> Unit = { SwingUtilities.invokeLater { current() } }

    val file = remember(bytes) {
        try {
            File.createTempFile("control_z_intro", ".mp4").apply {
                deleteOnExit()
                writeBytes(bytes)
            }
        } catch (e: Exception) {
            null
        }
    }
    // Crear el JFXPanel arranca JavaFX. Si JavaFX no está disponible, se sigue al menú.
    val panel = remember {
        try {
            JFXPanel()
        } catch (e: Throwable) {
            null
        }
    }
    if (file == null || panel == null) {
        LaunchedEffect(Unit) { current() }
        return
    }

    DisposableEffect(file, panel) {
        var player: MediaPlayer? = null
        Platform.runLater {
            try {
                val mp = MediaPlayer(Media(file.toURI().toString()))
                mp.volume = volume.toDouble().coerceIn(0.0, 1.0)
                mp.setOnEndOfMedia { finish() }
                mp.setOnError { finish() }
                val view = MediaView(mp).apply { isPreserveRatio = true }
                val scene = Scene(StackPane(view), javafx.scene.paint.Color.BLACK)
                view.fitWidthProperty().bind(scene.widthProperty())
                view.fitHeightProperty().bind(scene.heightProperty())
                scene.setOnMouseClicked { finish() }
                scene.setOnKeyPressed { finish() }
                panel.scene = scene
                player = mp
                mp.play()
            } catch (e: Throwable) {
                finish()
            }
        }
        onDispose {
            Platform.runLater {
                player?.stop()
                player?.dispose()
                panel.scene = null
                file.delete()
                // La animación solo sale al abrir el juego: se apaga JavaFX para que no impida cerrar la app.
                Platform.exit()
            }
        }
    }

    SwingPanel(factory = { panel.apply { background = java.awt.Color.BLACK } }, modifier = modifier)
}
