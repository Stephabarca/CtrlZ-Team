package com.example.videojuego.report

import com.example.videojuego.data.content.GameContent
import com.example.videojuego.data.save.SaveGame

/** Cómo respondió el jugador en una noche. */
data class NightReport(val order: Int, val title: String, val healthy: Int, val risky: Int, val timedOut: Int) {
    val total: Int get() = healthy + risky + timedOut
}

enum class Verdict { EXCELENTE, BUENO, A_MEJORAR }

/** Informe final de cómo se relacionó el jugador a lo largo de las noches. */
data class RelationshipReport(
    val nights: List<NightReport>,
    val strengths: List<String>,
    val risks: List<String>,
    val tips: List<String>,
    val healthyRatio: Float,
    val verdict: Verdict,
)

object ReportBuilder {
    const val EXCELLENT_MIN = 0.8f
    const val GOOD_MIN = 0.55f

    fun verdictFor(ratio: Float): Verdict = when {
        ratio >= EXCELLENT_MIN -> Verdict.EXCELENTE
        ratio >= GOOD_MIN -> Verdict.BUENO
        else -> Verdict.A_MEJORAR
    }

    fun build(content: GameContent, save: SaveGame?): RelationshipReport {
        val answers = save?.answers.orEmpty().associateBy { it.questionId }
        val healthyMotives = mutableMapOf<String, Int>()
        val riskyMotives = mutableMapOf<String, Int>()
        val tips = LinkedHashSet<String>()

        val nights = content.nights.sortedBy { it.order }.map { night ->
            var healthy = 0
            var risky = 0
            var timedOut = 0
            night.questions.forEach { q ->
                val record = answers[q.id] ?: return@forEach
                if (record.timedOut || record.answerId == null) {
                    timedOut++
                    return@forEach
                }
                val answer = q.answers.firstOrNull { it.id == record.answerId } ?: return@forEach
                val motive = content.motive(answer.motive)
                // Sin dato del motivo se usa el efecto en la afinidad para decidir si fue sano.
                val isHealthy = motive?.healthy ?: (answer.affinityDelta > 0)
                val label = motive?.label ?: answer.motive
                if (isHealthy) {
                    healthy++
                    healthyMotives[label] = (healthyMotives[label] ?: 0) + 1
                } else {
                    risky++
                    riskyMotives[label] = (riskyMotives[label] ?: 0) + 1
                    if (answer.feedback.isNotBlank()) tips += answer.feedback
                }
            }
            NightReport(night.order, night.title, healthy, risky, timedOut)
        }

        val total = nights.sumOf { it.total }
        val ratio = if (total == 0) 0f else nights.sumOf { it.healthy }.toFloat() / total
        return RelationshipReport(
            nights = nights,
            strengths = healthyMotives.entries.sortedByDescending { it.value }.map { it.key }.take(3),
            risks = riskyMotives.entries.sortedByDescending { it.value }.map { it.key }.take(3),
            tips = tips.take(3),
            healthyRatio = ratio,
            verdict = verdictFor(ratio),
        )
    }
}
