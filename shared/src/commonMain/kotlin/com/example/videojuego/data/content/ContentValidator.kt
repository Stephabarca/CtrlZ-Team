package com.example.videojuego.data.content

/**
 * Revisa la integridad del contenido. Devuelve la lista de problemas (vacía = todo bien).
 * También hace cumplir el checklist de relaciones sanas (IP-28): una conducta de riesgo nunca suma afinidad.
 */
object ContentValidator {

    fun validate(content: GameContent): List<String> {
        val errors = mutableListOf<String>()
        val cfg = content.config

        if (!(cfg.minAffinity <= cfg.startAffinity && cfg.startAffinity <= cfg.maxAffinity)) {
            errors += "config: se espera minAffinity <= startAffinity <= maxAffinity"
        }
        if (cfg.stages.none { it.minValue == cfg.minAffinity }) {
            errors += "config: debe haber una etapa que empiece en minAffinity"
        }

        val charIds = content.characters.map { it.id }
        if (charIds.size != 3) errors += "personajes: el juego tiene 3 supervivientes y hay ${charIds.size}"
        if (charIds.size != charIds.toSet().size) errors += "personajes: IDs duplicados"

        val motiveIds = content.motives.map { it.id }
        if (motiveIds.size != motiveIds.toSet().size) errors += "motivos: IDs duplicados"

        val minigameIds = content.minigames.map { it.id }
        content.minigames.forEach { m ->
            if (m.characterId !in charIds) errors += "minijuego ${m.id}: personaje inexistente '${m.characterId}'"
            if (m.maxAffinity !in 1..20) {
                errors += "minijuego ${m.id}: maxAffinity debe estar entre 1 y 20 para no reemplazar las decisiones"
            }
        }

        val speakers = charIds + "narrator"
        val allAnswerIds = mutableListOf<String>()
        val orders = content.nights.map { it.order }
        if (orders.size != orders.toSet().size) errors += "noches: orden duplicado"

        content.nights.forEach { night ->
            val at = "noche ${night.id}"
            if (night.characterId !in charIds) errors += "$at: personaje inexistente '${night.characterId}'"
            if (night.minigameId !in minigameIds) errors += "$at: minijuego inexistente '${night.minigameId}'"
            if (night.timeLimitSec <= 0) errors += "$at: timeLimitSec debe ser mayor que 0"
            if (night.answerWeight <= 0.0) errors += "$at: answerWeight debe ser mayor que 0"
            if (night.questions.isEmpty()) errors += "$at: no tiene preguntas"
            (night.intro + night.outro).forEach {
                if (it.speakerId !in speakers) errors += "$at: hablante desconocido '${it.speakerId}'"
            }
            night.questions.forEach { q ->
                if (q.speakerId !in speakers) errors += "${q.id}: hablante desconocido '${q.speakerId}'"
                if (q.answers.size < 3) errors += "${q.id}: necesita al menos 3 respuestas"
                if (q.answers.none { it.affinityDelta > 0 }) errors += "${q.id}: no tiene ninguna respuesta positiva"
                q.answers.forEach { a ->
                    allAnswerIds += a.id
                    if (a.text.isBlank() || a.feedback.isBlank()) errors += "${a.id}: texto o retroalimentación vacíos"
                    if (kotlin.math.abs(a.affinityDelta) > 15) errors += "${a.id}: |affinityDelta| debe ser 15 o menos"
                    val motive = content.motive(a.motive)
                    if (motive == null) {
                        errors += "${a.id}: motivo desconocido '${a.motive}'"
                    } else if (!motive.healthy && a.affinityDelta > 0) {
                        errors += "${a.id}: la conducta de riesgo '${a.motive}' no puede sumar afinidad"
                    } else if (motive.healthy && a.affinityDelta < 0) {
                        errors += "${a.id}: la conducta saludable '${a.motive}' no puede restar afinidad"
                    }
                }
            }
        }
        if (allAnswerIds.size != allAnswerIds.toSet().size) errors += "respuestas: IDs duplicados"

        if (content.endings.size < 3) errors += "finales: se requieren al menos 3"
        if (content.endings.none { it.rule.minCharactersAbove == 0 && it.rule.noOneBelow == 0 }) {
            errors += "finales: falta un final por defecto (regla sin condiciones)"
        }
        return errors
    }
}
