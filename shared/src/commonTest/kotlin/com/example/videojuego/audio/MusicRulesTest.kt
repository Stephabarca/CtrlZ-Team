package com.example.videojuego.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MusicRulesTest {
    @Test
    fun menuAndCredits_playMenuMusic() {
        assertEquals(MusicTrack.MENU, MusicRules.trackFor("menu"))
        assertEquals(MusicTrack.MENU, MusicRules.trackFor("credits"))
    }

    @Test
    fun settings_followsTheScreenItWasOpenedFrom() {
        assertEquals(MusicTrack.MENU, MusicRules.trackFor("settings", settingsReturn = "menu"))
        assertNull(MusicRules.trackFor("settings", settingsReturn = "world"))
    }

    @Test
    fun gameplayScreens_areSilent() {
        for (s in listOf("intro", "world", "round", "fishing", "fogata", "summary")) {
            assertNull(MusicRules.trackFor(s), s)
        }
    }

    @Test
    fun everyTrackHasAFile() {
        MusicTrack.entries.forEach { assertEquals(true, it.path.startsWith("files/music/")) }
    }
}
