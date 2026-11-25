package com.example.videojuego.finale

import com.example.videojuego.data.save.InMemorySaveStorage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EndingsTest {
    @Test
    fun parseIgnoresUnknownAndSpaces() {
        assertEquals(setOf("lucas", "lila"), Endings.parse(" lucas, lila ,nadie,"))
        assertTrue(Endings.parse(null).isEmpty())
        assertTrue(Endings.parse("").isEmpty())
    }

    @Test
    fun encodeKeepsStableOrder() {
        assertEquals("valentina,lila", Endings.encode(setOf("lila", "valentina")))
        assertEquals("lucas,amistad", Endings.encode(setOf("amistad", "lucas")))
        assertEquals(setOf("amistad"), Endings.parse("amistad,otro"))
    }

    @Test
    fun recordAccumulatesWithoutDuplicates() {
        val repo = EndingsRepository(InMemorySaveStorage())
        assertTrue(repo.seen().isEmpty())
        repo.record("lucas")
        repo.record("lucas")
        repo.record("lila")
        assertEquals(setOf("lucas", "lila"), repo.seen())
    }
}
