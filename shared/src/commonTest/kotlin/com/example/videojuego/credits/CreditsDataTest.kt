package com.example.videojuego.credits

import kotlin.test.Test
import kotlin.test.assertEquals
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
    fun everyRoleHasATitleAndSomeone() {
        CreditsData.departments.forEach { department ->
            assertTrue(department.title.isNotBlank())
            assertTrue(department.roles.isNotEmpty(), department.title)
            department.roles.forEach { role ->
                assertTrue(role.title.isNotBlank(), department.title)
                assertTrue(role.names.isNotEmpty() && role.names.all { it.isNotBlank() }, role.title)
            }
        }
    }

    @Test
    fun theTeamSectionIsKept() {
        assertEquals("EQUIPO", CreditsData.teamSection.title)
        assertEquals(listOf("Equipo CtrlZ"), CreditsData.teamSection.lines)
    }

    @Test
    fun departmentsGoInOrder() {
        assertEquals(
            listOf("DESARROLLO", "ARTE", "MÚSICA", "MENCIONES ESPECIALES", "MENTORES"),
            CreditsData.departments.map { it.title },
        )
    }

    @Test
    fun theWholeTeamAndMentorsAppear() {
        val everyone = CreditsData.team.toSet()
        listOf("Steph", "Luis", "Cris", "Kevin", "Beatriz").forEach { assertTrue(it in everyone, it) }
    }
}
