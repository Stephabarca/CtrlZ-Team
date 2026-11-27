package com.example.videojuego.audio

import com.example.videojuego.world.DayPhase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MusicRulesTest {
    @Test
    fun menuPlaysItsMusic_creditsPlayTheirSong() {
        assertEquals(MusicTrack.MENU, MusicRules.trackFor("menu"))
        assertEquals(MusicTrack.CREDITS, MusicRules.trackFor("credits"))
    }

    @Test
    fun world_playsBeachDayOrNight() {
        assertEquals(MusicTrack.BEACH_DAY, MusicRules.trackFor("world", phase = DayPhase.DAY))
        assertEquals(MusicTrack.BEACH_NIGHT, MusicRules.trackFor("world", phase = DayPhase.NIGHT))
    }

    @Test
    fun round_playsTheClock() {
        assertEquals(MusicTrack.ROUND_CLOCK, MusicRules.trackFor("round"))
    }

    @Test
    fun settings_followsTheScreenItWasOpenedFrom() {
        assertEquals(MusicTrack.MENU, MusicRules.trackFor("settings", settingsReturn = "menu"))
        assertEquals(MusicTrack.BEACH_NIGHT, MusicRules.trackFor("settings", settingsReturn = "world", phase = DayPhase.NIGHT))
    }

    @Test
    fun buildMinigame_playsTheNightBeach() {
        assertEquals(MusicTrack.BEACH_NIGHT, MusicRules.trackFor("build"))
    }

    @Test
    fun storm_playsTheStorm() {
        assertEquals(MusicTrack.STORM, MusicRules.trackFor("storm"))
    }

    @Test
    fun finale_playsTheSea() {
        assertEquals(MusicTrack.SEA, MusicRules.trackFor("finale"))
    }

    @Test
    fun otherScreens_areSilent() {
        // "splash": la animación de inicio trae su propio sonido; la música del menú empieza al terminar.
        for (s in listOf("splash", "intro", "fishing", "fogata", "summary")) {
            assertNull(MusicRules.trackFor(s), s)
        }
    }

    @Test
    fun everyTrackHasAFile() {
        MusicTrack.entries.forEach {
            assertEquals(true, it.path.startsWith("files/music/") || it.path.startsWith("files/sounds/"), it.name)
        }
    }

    @Test
    fun everyEffectHasAFile() {
        SoundEffect.entries.forEach { assertEquals(true, it.path.startsWith("files/sounds/"), it.name) }
    }

    @Test
    fun onlyTheTwoSongs_areSongs() {
        assertEquals(setOf(MusicTrack.MENU, MusicTrack.CREDITS), MusicTrack.entries.filter { it.isSong }.toSet())
        // Las canciones están en music/ y los ambientes en sounds/.
        MusicTrack.entries.forEach { assertEquals(it.isSong, it.path.startsWith("files/music/"), it.name) }
    }

    @Test
    fun songsFollowMusicVolume_ambientsFollowEffectsVolume() {
        assertEquals(0.3f, MusicRules.volumeFor(MusicTrack.MENU, musicVolume = 0.3f, sfxVolume = 0.9f))
        assertEquals(0.3f, MusicRules.volumeFor(MusicTrack.CREDITS, musicVolume = 0.3f, sfxVolume = 0.9f))
        for (ambient in listOf(MusicTrack.BEACH_DAY, MusicTrack.BEACH_NIGHT, MusicTrack.ROUND_CLOCK, MusicTrack.SEA, MusicTrack.STORM)) {
            assertEquals(0.9f, MusicRules.volumeFor(ambient, musicVolume = 0.3f, sfxVolume = 0.9f), ambient.name)
        }
    }
}
