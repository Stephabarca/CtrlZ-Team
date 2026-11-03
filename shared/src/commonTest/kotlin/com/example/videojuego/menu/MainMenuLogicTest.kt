package com.example.videojuego.menu

import com.example.videojuego.data.save.LoadResult
import com.example.videojuego.data.save.SaveGame
import com.example.videojuego.data.save.WorldSave
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MainMenuLogicTest {

    @Test
    fun whileLoading_nothingCanBeContinued() {
        val info = MainMenuLogic.info(null)
        assertFalse(info.canContinue)
        assertFalse(info.corrupt)
        assertNull(info.description)
    }

    @Test
    fun withoutASave_onlyNewGameIsOffered() {
        val info = MainMenuLogic.info(LoadResult.NoSave)
        assertFalse(info.canContinue)
        assertFalse(info.corrupt)
        assertFalse(info.newGameNeedsConfirmation)
    }

    @Test
    fun aSave_enablesContinueAndShowsTheDay() {
        val save = SaveGame(world = WorldSave(day = 2))
        val info = MainMenuLogic.info(LoadResult.Loaded(save))
        assertTrue(info.canContinue)
        assertEquals("Día 2", info.description)
    }

    @Test
    fun aSaveWithoutWorld_stillCanBeContinued() {
        val info = MainMenuLogic.info(LoadResult.Loaded(SaveGame()))
        assertTrue(info.canContinue)
        assertEquals(MainMenuLogic.SAVED_GAME, info.description)
    }

    @Test
    fun startingOver_asksForConfirmationOnlyWhenThereIsASave() {
        assertTrue(MainMenuLogic.info(LoadResult.Loaded(SaveGame())).newGameNeedsConfirmation)
        assertFalse(MainMenuLogic.info(LoadResult.NoSave).newGameNeedsConfirmation)
        assertFalse(MainMenuLogic.info(LoadResult.Corrupt).newGameNeedsConfirmation)
    }

    @Test
    fun aCorruptSave_isReportedAndCannotBeContinued() {
        val info = MainMenuLogic.info(LoadResult.Corrupt)
        assertTrue(info.corrupt)
        assertFalse(info.canContinue)
        assertNull(info.description)
    }
}
