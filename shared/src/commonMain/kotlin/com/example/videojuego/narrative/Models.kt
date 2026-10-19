package com.example.videojuego.narrative

/** Quién habla en una línea. El narrador no tiene sprite ni pestaña de nombre. */
enum class Speaker(val displayName: String, val isSurvivor: Boolean) {
    NARRATOR("", false),
    VALENTINA("Valentina", true),
    LUCAS("Lucas", true),
    LILA("Lila", true),
}

/** Apoyo visual opcional que se muestra durante el tutorial. */
enum class TutorialHint { NONE, TIMER, AFFINITY }

data class DialogueLine(
    val speaker: Speaker,
    val text: String,
    /** Las líneas marcadas como tutorial se pueden omitir. */
    val isTutorial: Boolean = false,
    val hint: TutorialHint = TutorialHint.NONE,
)

/** Resultado de la Fase 1, que recibirá el controlador de fases (IP-29). */
data class IntroResult(
    val skippedTutorial: Boolean,
    val metSurvivors: Set<Speaker>,
)
