package com.example.videojuego.summary

import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.content.NightData
import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.minigame.fishing.FishingStats

/** Una pregunta de la ronda, con lo que respondió el jugador y por qué importa. */
data class QuestionReview(
    val prompt: String,
    /** Texto de la respuesta elegida, o null si se acabó el tiempo. */
    val chosenText: String?,
    val timedOut: Boolean,
    val affinityDelta: Int,
    val motiveLabel: String?,
    /** true = conducta sana, false = conducta de riesgo, null = no respondió. */
    val healthy: Boolean?,
    val feedback: String,
)

data class MinigameReview(
    val name: String,
    val score: Int,
    val affinityGranted: Int,
    val stats: FishingStats,
    val tips: List<String>,
)

data class NightSummary(
    val nightTitle: String,
    val characterName: String,
    val affinityBefore: Int,
    val affinityAfter: Int,
    val stageName: String,
    /** 0 a 1, para dibujar los corazones. */
    val affinityFraction: Float,
    val questions: List<QuestionReview>,
    val healthyCount: Int,
    val answeredCount: Int,
    /** Segundos promedio que tardó en responder (solo respuestas dadas). */
    val averageSeconds: Float?,
    val questionsAffinity: Int,
    val minigame: MinigameReview?,
    /** Lo que se practicó con este personaje. */
    val learning: String,
    /** Lo que conviene mejorar: la retroalimentación de las respuestas de riesgo o sin responder. */
    val improvements: List<String>,
    val headline: String,
) {
    val totalAffinityChange: Int get() = affinityAfter - affinityBefore
}

/** Arma el resumen de una noche. Es pura: solo lee contenido y progreso, así que se prueba sin pantallas. */
object NightSummaryBuilder {

    fun build(
        content: GameContent,
        night: NightData,
        saveAtNightStart: SaveGame,
        saveAtEnd: SaveGame,
        fishing: FishingStats?,
        timeoutMessage: String = DEFAULT_TIMEOUT_TIP,
    ): NightSummary {
        val character = content.character(night.characterId)
        val start = content.config.startAffinity
        val before = saveAtNightStart.affinity[night.characterId] ?: start
        val after = saveAtEnd.affinity[night.characterId] ?: start

        val reviews = night.questions.mapNotNull { question ->
            val record = saveAtEnd.answers.lastOrNull { it.questionId == question.id } ?: return@mapNotNull null
            val answer = question.answers.firstOrNull { it.id == record.answerId }
            val motive = answer?.let { content.motive(it.motive) }
            QuestionReview(
                prompt = question.prompt,
                chosenText = answer?.text,
                timedOut = record.timedOut,
                affinityDelta = record.affinityDelta,
                motiveLabel = motive?.label,
                healthy = motive?.healthy,
                feedback = answer?.feedback ?: timeoutMessage,
            )
        }

        val answered = reviews.filter { !it.timedOut }
        val times = night.questions.mapNotNull { q ->
            saveAtEnd.answers.lastOrNull { it.questionId == q.id && !it.timedOut }?.timeSpentMs
        }
        val minigameData = content.minigames.firstOrNull { it.id == night.minigameId }
        val granted = saveAtEnd.minigameResults.lastOrNull { it.minigameId == night.minigameId }?.affinityGranted ?: 0
        val minigame = if (fishing != null && minigameData != null) {
            MinigameReview(minigameData.name, fishing.score, granted, fishing, fishingTips(fishing))
        } else {
            null
        }

        val improvements = reviews.filter { it.healthy != true }.map { it.feedback }.distinct()
        val healthy = reviews.count { it.healthy == true }
        val questionsAffinity = reviews.sumOf { it.affinityDelta }

        return NightSummary(
            nightTitle = night.title,
            characterName = character.name,
            affinityBefore = before,
            affinityAfter = after,
            stageName = ProgressRules.stageFor(after, content.config).name,
            affinityFraction = fraction(after, content),
            questions = reviews,
            healthyCount = healthy,
            answeredCount = answered.size,
            averageSeconds = if (times.isEmpty()) null else times.average().toFloat() / 1000f,
            questionsAffinity = questionsAffinity,
            minigame = minigame,
            learning = character.learning,
            improvements = improvements,
            headline = headline(reviews.size, healthy, after - before, character.name),
        )
    }

    private fun fraction(affinity: Int, content: GameContent): Float {
        val range = content.config.maxAffinity - content.config.minAffinity
        return if (range <= 0) 0f else ((affinity - content.config.minAffinity).toFloat() / range).coerceIn(0f, 1f)
    }

    /** Consejos de la pesca. Hablan de paciencia y atención, que es lo que se practica al compartir un rato con alguien. */
    fun fishingTips(stats: FishingStats): List<String> = buildList {
        if (stats.early > 0) {
            add("Jalaste antes de tiempo ${times(stats.early)}. Apurar las cosas espanta lo bueno: con las personas también hay que saber esperar.")
        }
        if (stats.lost > 0) {
            add("Se escapó${if (stats.lost == 1) "" else "n"} ${stats.lost} pez${if (stats.lost == 1) "" else "es"} por no estar atento. Estar presente en el momento importa.")
        }
        if (stats.weak > 0) {
            add("${stats.weak} captura${if (stats.weak == 1) "" else "s"} fuera de tiempo. Apunta al centro de la zona dorada.")
        }
        if (stats.perfect >= 3) {
            add("Tuviste buen ritmo: ${stats.perfect} capturas perfectas. Esperar y actuar en el momento justo funciona.")
        }
        if (stats.attempts == 0) {
            add("No hubo ningún intento. La próxima vez toca JALAR cuando el corcho se hunda.")
        }
        if (isEmpty()) add("Pesca tranquila y sin errores. Buen trabajo.")
    }

    private fun times(n: Int) = if (n == 1) "1 vez" else "$n veces"

    private fun headline(total: Int, healthy: Int, delta: Int, name: String): String = when {
        total == 0 -> "Noche con $name."
        healthy == total && delta > 0 -> "¡Gran noche! Todas tus respuestas ayudaron a construir confianza con $name."
        healthy * 2 >= total && delta >= 0 -> "Buena noche con $name. Hay cosas por mejorar, pero el vínculo avanzó."
        delta >= 0 -> "Noche con altibajos. Revisa lo que pasó para la próxima vez."
        else -> "Esta noche el vínculo con $name se resintió. Mira qué puedes hacer distinto."
    }

    const val DEFAULT_TIMEOUT_TIP = "Se acabó el tiempo. No responder también comunica algo: intenta decidir con más calma pero sin quedarte callado."
}
