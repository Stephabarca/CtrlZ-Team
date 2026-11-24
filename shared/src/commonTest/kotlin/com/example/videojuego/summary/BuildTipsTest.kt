package com.example.videojuego.summary

import com.example.videojuego.minigame.build.BuildStats
import kotlin.test.Test
import kotlin.test.assertTrue

class BuildTipsTest {

    @Test
    fun tips_mentionTrapsWrongPiecesAndTimeouts() {
        val tips = NightSummaryBuilder.buildingTips(BuildStats(40, 1, 8, 2, 3, 4, 3))
        assertTrue(tips.any { it.contains("3 piezas trampa") })
        assertTrue(tips.any { it.contains("2 piezas") && it.contains("no eran") })
        assertTrue(tips.any { it.contains("4 piezas solo") })
    }

    @Test
    fun tips_praiseTeamwork() {
        val tips = NightSummaryBuilder.buildingTips(BuildStats(90, 4, 25, 0, 0, 0, 12))
        assertTrue(tips.any { it.contains("4 fogatas") })
    }

    @Test
    fun tips_alwaysHaveAtLeastOne() {
        assertTrue(NightSummaryBuilder.buildingTips(BuildStats(0, 0, 0, 0, 0, 0, 0)).isNotEmpty())
        assertTrue(NightSummaryBuilder.buildingTips(BuildStats(60, 2, 9, 0, 0, 0, 5)).isNotEmpty())
    }
}
