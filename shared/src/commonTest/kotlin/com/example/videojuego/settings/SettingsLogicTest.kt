package com.example.videojuego.settings

import com.example.videojuego.data.save.GameSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsLogicTest {

    @Test
    fun speed_endpoints() {
        assertEquals(0f, SettingsLogic.speedFromDelay(100), 0.001f)
        assertEquals(1f, SettingsLogic.speedFromDelay(10), 0.001f)
        assertEquals(0.5f, SettingsLogic.speedFromDelay(55), 0.001f)
    }

    @Test
    fun speed_valuesOutsideTheRangeAreClamped() {
        assertEquals(0f, SettingsLogic.speedFromDelay(500), 0.001f)
        assertEquals(1f, SettingsLogic.speedFromDelay(0), 0.001f)
    }

    @Test
    fun delay_endpoints() {
        assertEquals(100L, SettingsLogic.delayFromSpeed(0f))
        assertEquals(10L, SettingsLogic.delayFromSpeed(1f))
        assertEquals(55L, SettingsLogic.delayFromSpeed(0.5f))
        assertEquals(100L, SettingsLogic.delayFromSpeed(-3f))
        assertEquals(10L, SettingsLogic.delayFromSpeed(9f))
    }

    @Test
    fun speedAndDelay_roundTrip() {
        for (ms in listOf(10L, 30L, 55L, 80L, 100L)) {
            assertEquals(ms, SettingsLogic.delayFromSpeed(SettingsLogic.speedFromDelay(ms)))
        }
    }

    @Test
    fun changingOneSetting_keepsTheOthers() {
        val base = GameSettings(textDelayMs = 30, musicVolume = 0.3f, sfxVolume = 0.6f)
        val faster = SettingsLogic.withTextSpeed(base, 1f)
        assertEquals(10L, faster.textDelayMs)
        assertEquals(0.3f, faster.musicVolume, 0.001f)
        assertEquals(0.6f, faster.sfxVolume, 0.001f)
        assertEquals(0.9f, SettingsLogic.withMusic(base, 0.9f).musicVolume, 0.001f)
        assertEquals(0.1f, SettingsLogic.withSfx(base, 0.1f).sfxVolume, 0.001f)
    }

    @Test
    fun volumes_areClampedToTheValidRange() {
        val base = GameSettings()
        assertEquals(1f, SettingsLogic.withMusic(base, 4f).musicVolume, 0.001f)
        assertEquals(0f, SettingsLogic.withSfx(base, -1f).sfxVolume, 0.001f)
    }

    @Test
    fun percent_isRounded() {
        assertEquals(0, SettingsLogic.percent(0f))
        assertEquals(80, SettingsLogic.percent(0.8f))
        assertEquals(100, SettingsLogic.percent(3f))
    }
}
