package com.example.videojuego.credits

import kotlin.test.Test
import kotlin.test.assertTrue

class CreditsDataTest {
    @Test
    fun everySectionHasATitleAndText() {
        assertTrue(CreditsData.sections.isNotEmpty())
        CreditsData.sections.forEach { section ->
            assertTrue(section.title.isNotBlank())
            assertTrue(section.lines.isNotEmpty() && section.lines.all { it.isNotBlank() })
        }
    }

    @Test
    fun teamIsNotEmpty() {
        assertTrue(CreditsData.team.isNotEmpty())
    }
}
