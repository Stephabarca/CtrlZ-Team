package com.example.videojuego.menu

import com.example.videojuego.data.save.GamePhase
import com.example.videojuego.data.save.LoadResult

/** Lo que el menú de inicio necesita saber de la partida guardada. */
data class MenuInfo(
    /** Se muestra CONTINUAR. */
    val canContinue: Boolean,
    /** Hay una partida pero está dañada: se avisa y solo se puede empezar de nuevo. */
    val corrupt: Boolean,
    /** Texto corto de la partida guardada, por ejemplo "Día 2". Null si no hay partida. */
    val description: String?,
) {
    /** Empezar de nuevo borra algo, así que hay que pedir confirmación. */
    val newGameNeedsConfirmation: Boolean get() = canContinue
}

object MainMenuLogic {

    /** [load] es null mientras todavía se está leyendo el guardado. */
    fun info(load: LoadResult?): MenuInfo = when (load) {
        // Partida terminada (ya se vio el final): no hay nada que continuar ni que perder al empezar de nuevo.
        is LoadResult.Loaded -> if (load.save.phase == GamePhase.ENDING) {
            MenuInfo(canContinue = false, corrupt = false, description = FINISHED_GAME)
        } else {
            MenuInfo(
                canContinue = true,
                corrupt = false,
                description = load.save.world?.let { "Día ${it.day}" } ?: SAVED_GAME,
            )
        }
        LoadResult.Corrupt -> MenuInfo(canContinue = false, corrupt = true, description = null)
        LoadResult.NoSave, null -> MenuInfo(canContinue = false, corrupt = false, description = null)
    }

    const val SAVED_GAME = "Partida guardada"
    const val FINISHED_GAME = "Partida terminada"
}
