package com.example.videojuego.demo

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DemoKeysTest {
    @Test
    fun mapsKeysToCommands() {
        assertEquals(DemoCommand.Night(2), DemoKeyMap.commandFor('2'))
        assertEquals(DemoCommand.Storm, DemoKeyMap.commandFor('s'))
        assertEquals(DemoCommand.Finale("amistad"), DemoKeyMap.commandFor('A'))
        assertEquals(DemoCommand.Finale(null), DemoKeyMap.commandFor('F'))
        assertNull(DemoKeyMap.commandFor('x'))
    }
}
