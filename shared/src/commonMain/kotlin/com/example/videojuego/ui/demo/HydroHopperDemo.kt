package com.example.videojuego.ui.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.videojuego.minigame.hydrohopper.HydroHopperStats
import com.example.videojuego.ui.minigame.HydroHopperScreen

/**
 * Pantalla de demostración aislada para probar el minijuego Hydro Hopper (Noche 3)
 * de forma independiente antes de conectarlo con las rondas Q&A y el resumen.
 */
@Composable
fun HydroHopperDemo(
    onFinished: (HydroHopperStats) -> Unit = {},
) {
    var lastStats by remember { mutableStateOf<HydroHopperStats?>(null) }

    HydroHopperScreen(
        durationSec = 25,
        onFinished = { stats ->
            lastStats = stats
            onFinished(stats)
        },
    )
}
