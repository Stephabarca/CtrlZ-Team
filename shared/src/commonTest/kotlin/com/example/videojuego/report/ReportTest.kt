package com.example.videojuego.report

import com.example.videojuego.data.TestFixtures
import com.example.videojuego.data.save.AnswerRecord
import com.example.videojuego.data.save.SaveGame
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReportTest {
    private val content = TestFixtures.content()

    private fun rec(q: String, a: String?, timedOut: Boolean = false) =
        AnswerRecord(q, a, 1000, timedOut, 0)

    @Test
    fun countsHealthyRiskyAndTimedOut() {
        val save = SaveGame(
            answers = listOf(rec("n1q1", "n1q1a1"), rec("n2q1", "n2q1a4"), rec("n3q1", null, true)),
        )
        val r = ReportBuilder.build(content, save)
        assertEquals(1, r.nights[0].healthy)
        assertEquals(1, r.nights[1].risky)
        assertEquals(1, r.nights[2].timedOut)
        assertEquals(listOf("Escucha"), r.strengths)
        assertEquals(listOf("Celos"), r.risks)
        assertEquals(listOf("mal"), r.tips)
    }

    @Test
    fun verdictThresholds() {
        assertEquals(Verdict.EXCELENTE, ReportBuilder.verdictFor(0.8f))
        assertEquals(Verdict.BUENO, ReportBuilder.verdictFor(0.6f))
        assertEquals(Verdict.A_MEJORAR, ReportBuilder.verdictFor(0.3f))
    }

    @Test
    fun emptySaveGivesZeroRatio() {
        val r = ReportBuilder.build(content, null)
        assertEquals(0f, r.healthyRatio)
        assertTrue(r.strengths.isEmpty())
    }
}
