package com.example.videojuego.history

import com.example.videojuego.story.model.Opcion
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AnswerHistoryTest {

    private fun option(id: String, effect: Map<String, Int> = emptyMap()) =
        Opcion(id = id, texto = "texto $id", siguienteEscenaId = "s", afinidad = effect)

    @Test
    fun answeredRecordKeepsTheFourData() {
        val history = AnswerHistory()
        val created = history.recordAnswer(
            roundId = "noche1",
            questionId = "q1",
            option = option("o1", mapOf("mara" to 5)),
            timeSpent = 2_500.milliseconds,
        )
        assertNotNull(created)

        val record = history.all().single()
        assertEquals("q1", record.questionId)          // pregunta
        assertEquals("o1", record.optionId)            // opción elegida
        assertEquals(2_500L, record.timeSpentMillis)   // tiempo empleado
        assertEquals(mapOf("mara" to 5), record.effect) // efecto
        assertEquals(AnswerStatus.Answered, record.status)
    }

    @Test
    fun timeoutIsRecordedAsNoAnswer() {
        val history = AnswerHistory()
        history.recordTimeout("noche1", "q2", 10.seconds)

        val record = history.all().single()
        assertEquals(AnswerStatus.TimedOut, record.status)
        assertTrue(record.isNoAnswer)
        assertNull(record.optionId)
        assertTrue(record.effect.isEmpty())
        assertEquals(10_000L, record.timeSpentMillis)
    }

    @Test
    fun aFullRoundKeepsEveryAnswerInOrder() {
        val history = AnswerHistory()
        history.recordAnswer("noche1", "q1", option("a"), 1.seconds)
        history.recordTimeout("noche1", "q2", 10.seconds)
        history.recordAnswer("noche1", "q3", option("c"), 3.seconds)

        assertEquals(listOf("q1", "q2", "q3"), history.all().map { it.questionId })
        assertEquals(2, history.count(AnswerStatus.Answered))
        assertEquals(1, history.count(AnswerStatus.TimedOut))
    }

    @Test
    fun aQuestionIsRecordedOnlyOnce() {
        val history = AnswerHistory()
        assertNotNull(history.recordAnswer("noche1", "q1", option("a"), 1.seconds))
        assertNull(history.recordTimeout("noche1", "q1", 10.seconds))
        assertNull(history.recordAnswer("noche1", "q1", option("b"), 2.seconds))

        assertEquals(1, history.all().size)
        assertEquals("a", history.all().single().optionId)
    }

    @Test
    fun sameQuestionInAnotherRoundIsAllowed() {
        val history = AnswerHistory()
        history.recordAnswer("noche1", "q1", option("a"), 1.seconds)
        history.recordAnswer("noche2", "q1", option("a"), 1.seconds)
        assertEquals(2, history.all().size)
    }

    @Test
    fun canQueryByRound() {
        val history = AnswerHistory()
        history.recordAnswer("noche1", "q1", option("a"), 1.seconds)
        history.recordAnswer("noche2", "q1", option("a"), 1.seconds)
        history.recordTimeout("noche2", "q2", 10.seconds)

        assertEquals(1, history.forRound("noche1").size)
        assertEquals(2, history.forRound("noche2").size)
        assertTrue(history.forRound("noche3").isEmpty())
    }

    @Test
    fun totalEffectSumsPerCharacter() {
        val history = AnswerHistory()
        history.recordAnswer("noche1", "q1", option("a", mapOf("mara" to 5)), 1.seconds)
        history.recordAnswer("noche1", "q2", option("b", mapOf("mara" to -3, "leo" to 2)), 1.seconds)
        history.recordTimeout("noche1", "q3", 10.seconds) // no suma efecto
        history.recordAnswer("noche2", "q1", option("a", mapOf("leo" to 4)), 1.seconds)

        assertEquals(mapOf("mara" to 2, "leo" to 6), history.totalEffect())
        assertEquals(mapOf("mara" to 2, "leo" to 2), history.totalEffect("noche1"))
    }

    @Test
    fun clearEmptiesTheHistory() {
        val history = AnswerHistory()
        history.recordAnswer("noche1", "q1", option("a"), 1.seconds)
        history.clear()
        assertTrue(history.all().isEmpty())
        assertNotNull(history.recordAnswer("noche1", "q1", option("a"), 1.seconds))
    }

    @Test
    fun recordSurvivesJsonRoundTrip() {
        val record = AnswerRecord.answered("noche1", "q1", "o1", 1_200, mapOf("mara" to 5))
        val json = Json.encodeToString(record)
        assertEquals(record, Json.decodeFromString<AnswerRecord>(json))

        val timeout = AnswerRecord.timedOut("noche1", "q2", 10_000)
        assertEquals(timeout, Json.decodeFromString<AnswerRecord>(Json.encodeToString(timeout)))
    }

    @Test
    fun inconsistentRecordsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            AnswerRecord("n", "q", null, 1, emptyMap(), AnswerStatus.Answered)
        }
        assertFailsWith<IllegalArgumentException> {
            AnswerRecord("n", "q", "o", 1, emptyMap(), AnswerStatus.TimedOut)
        }
        assertFailsWith<IllegalArgumentException> {
            AnswerRecord("n", "q", null, 1, mapOf("mara" to 1), AnswerStatus.TimedOut)
        }
        assertFailsWith<IllegalArgumentException> {
            AnswerRecord.answered("n", "q", "o", -1, emptyMap())
        }
    }
}
