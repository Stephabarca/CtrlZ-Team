package com.example.videojuego.data

import com.example.videojuego.data.rules.ProgressRules
import com.example.videojuego.data.save.GamePhase
import com.example.videojuego.data.save.GameSettings
import com.example.videojuego.data.save.InMemorySaveStorage
import com.example.videojuego.data.save.LoadResult
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.data.save.SaveRepository
import com.example.videojuego.data.save.SettingsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveRepositoryTest {

    private val content = TestFixtures.content()

    @Test
    fun load_withoutSave_returnsNoSave() {
        assertEquals(LoadResult.NoSave, SaveRepository(InMemorySaveStorage()).load())
    }

    @Test
    fun saveThenLoad_restoresPhaseAffinityAndAnswers() {
        val repo = SaveRepository(InMemorySaveStorage())
        val night = content.nights[0]
        val played = ProgressRules.recordAnswer(
            content, ProgressRules.newGame(content), night, night.questions[0], night.questions[0].answers[0], 2_500,
        ).copy(phase = GamePhase.NIGHT_1, skippedTutorial = true)

        repo.save(played)

        val loaded = repo.load()
        assertTrue(loaded is LoadResult.Loaded)
        assertEquals(played, loaded.save)
        assertTrue(repo.hasSave())
    }

    @Test
    fun load_withCorruptData_returnsCorrupt() {
        val storage = InMemorySaveStorage().apply { write("{esto no es json") }
        assertEquals(LoadResult.Corrupt, SaveRepository(storage).load())
    }

    @Test
    fun load_withOtherVersion_returnsCorrupt() {
        val repo = SaveRepository(InMemorySaveStorage())
        repo.save(SaveGame(version = 99))
        assertEquals(LoadResult.Corrupt, repo.load())
    }

    @Test
    fun clear_removesTheSave() {
        val repo = SaveRepository(InMemorySaveStorage())
        repo.save(ProgressRules.newGame(content))
        repo.clear()
        assertEquals(LoadResult.NoSave, repo.load())
        assertFalse(repo.hasSave())
    }

    @Test
    fun settings_defaultsWhenEmptyAndClampsWhenOutOfRange() {
        val repo = SettingsRepository(InMemorySaveStorage())
        assertEquals(GameSettings(), repo.load())

        repo.save(GameSettings(textDelayMs = 5_000L, musicVolume = 3f, sfxVolume = -1f))
        val loaded = repo.load()
        assertEquals(100L, loaded.textDelayMs)
        assertEquals(1f, loaded.musicVolume)
        assertEquals(0f, loaded.sfxVolume)
    }

    @Test
    fun settings_corruptData_fallsBackToDefaults() {
        val storage = InMemorySaveStorage().apply { write("basura") }
        assertEquals(GameSettings(), SettingsRepository(storage).load())
    }
}
