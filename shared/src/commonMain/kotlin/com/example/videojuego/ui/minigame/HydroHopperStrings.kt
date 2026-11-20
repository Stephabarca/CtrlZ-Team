package com.example.videojuego.ui.minigame

object HydroHopperStrings {
    const val Title = "MINIJUEGO: RESCATE EN EL MAR"
    const val Subtitle = "¡Esquiva los obstáculos y llega hasta el barco de rescate junto a los supervivientes!"
    const val Start = "¡COMENZAR RESCATE!"
    const val InstructionsTitle = "INSTRUCCIONES:"
    const val Inst1 = "• Arrastra o toca a la izquierda / derecha (o usa A/D / Flechas) para mover la balsa."
    const val Inst2 = "• Esquiva troncos, rocas y boyas que flotan en el mar."
    const val Inst3 = "• Avanza el 100% de la distancia para alcanzar el barco de rescate."
    const val Continue = "CONTINUAR"

    const val ShipAhead = "¡EL BARCO DE RESCATE ESTÁ CERCA!"
    const val VictoryTitle = "¡RESCATADOS JUNTOS!"
    const val VictorySubtitle = "¡La balsa alcanzó el barco! Valentina, Lucas, Lila y tú han sido rescatados a salvo."

    const val ScoreLabel = "PUNTAJE"
    const val DistanceLabel = "DISTANCIA AL BARCO"

    fun scoreText(score: Int) = "$ScoreLabel: $score"
    fun distanceText(progress: Float) = "${(progress * 100).toInt()}%"
}
