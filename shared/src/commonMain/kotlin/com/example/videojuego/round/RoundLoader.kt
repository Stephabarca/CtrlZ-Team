package com.example.videojuego.round

import com.example.videojuego.data.content.ContentRepository
import com.example.videojuego.data.content.ContentValidator
import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.data.save.SaveGame
import kotlin.coroutines.cancellation.CancellationException

sealed interface RoundLoadResult {
    data class Ready(val controller: QuestionRoundController) : RoundLoadResult
    /** Error controlado: [message] se puede mostrar al jugador. */
    data class Error(val message: String) : RoundLoadResult
}

object RoundLoader {
    const val GENERIC_ERROR = "No se pudieron cargar las preguntas de esta noche."

    /** Revisa el banco de preguntas y arma el controlador. Es pura, así que se prueba sin archivos. */
    fun prepare(content: GameContent, nightId: String, save: SaveGame?): RoundLoadResult {
        val night = content.nights.firstOrNull { it.id == nightId }
            ?: return RoundLoadResult.Error("No existe la noche '$nightId'.")
        if (night.questions.isEmpty()) {
            return RoundLoadResult.Error("Esta noche no tiene preguntas.")
        }
        night.questions.firstOrNull { it.answers.size !in 3..4 }?.let {
            return RoundLoadResult.Error("La pregunta ${it.id} debe tener 3 o 4 respuestas.")
        }
        val problems = ContentValidator.validate(content)
        if (problems.isNotEmpty()) {
            return RoundLoadResult.Error("El contenido del juego tiene errores: ${problems.first()}")
        }
        return RoundLoadResult.Ready(
            QuestionRoundController(content, night, save ?: ProgressRules.newGame(content)),
        )
    }

    /** Lee los JSON y prepara la ronda. Cualquier archivo faltante o dañado se convierte en un error controlado. */
    suspend fun load(repository: ContentRepository, nightId: String, save: SaveGame?): RoundLoadResult =
        try {
            prepare(repository.load(), nightId, save)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            RoundLoadResult.Error(GENERIC_ERROR)
        }
}
