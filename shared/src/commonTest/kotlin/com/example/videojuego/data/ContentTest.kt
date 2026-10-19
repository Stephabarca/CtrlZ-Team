package com.example.videojuego.data

import com.example.videojuego.data.content.AnswerData
import com.example.videojuego.data.content.ContentValidator
import com.example.videojuego.data.content.NightData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContentTest {

    private val content = TestFixtures.content()

    @Test
    fun validContent_hasNoErrors() {
        assertEquals(emptyList(), ContentValidator.validate(content))
    }

    @Test
    fun validator_detectsUnknownCharacter() {
        val bad = content.copy(nights = content.nights.map { if (it.order == 1) it.copy(characterId = "nadie") else it })
        assertTrue(ContentValidator.validate(bad).any { "personaje inexistente" in it })
    }

    @Test
    fun validator_rejectsRiskBehaviorThatAddsAffinity() {
        // Checklist de relaciones sanas (IP-28): los celos nunca pueden sumar afinidad.
        val night = content.nights[0]
        val tampered = night.questions[0].answers.map {
            if (it.motive == "celos") it.copy(affinityDelta = 5) else it
        }
        val bad = content.copy(nights = listOf(replaceAnswers(night, tampered)) + content.nights.drop(1))
        assertTrue(ContentValidator.validate(bad).any { "no puede sumar afinidad" in it })
    }

    @Test
    fun validator_detectsDuplicateAnswerIds() {
        val night = content.nights[0]
        val dup = night.questions[0].answers.map { it.copy(id = "n1q1a1") }
        val bad = content.copy(nights = listOf(replaceAnswers(night, dup)) + content.nights.drop(1))
        assertTrue(ContentValidator.validate(bad).any { "duplicados" in it })
    }

    @Test
    fun validator_requiresDefaultEnding() {
        val bad = content.copy(endings = content.endings.filter { it.id != "solos" })
        assertTrue(ContentValidator.validate(bad).any { "final por defecto" in it })
    }

    @Test
    fun json_decodesNightAndIgnoresUnknownFields() {
        val text = """
            {"id":"night9","order":9,"characterId":"lila","title":"t","backgroundId":"bg",
             "timeLimitSec":10,"answerWeight":1.5,"minigameId":"memoria","campoNuevo":123,
             "intro":[{"speakerId":"narrator","text":"hola"}],
             "questions":[{"id":"q","speakerId":"lila","prompt":"p","answers":[
               {"id":"a","text":"x","affinityDelta":10,"motive":"escucha","feedback":"f"}]}],
             "outro":[]}
        """.trimIndent()
        val night = GameJson.decodeFromString<NightData>(text)
        assertEquals(1.5, night.answerWeight)
        assertEquals("neutral", night.intro.single().expression) // valor por defecto
        assertEquals(10, night.questions.single().answers.single().affinityDelta)
    }

    private fun replaceAnswers(night: NightData, answers: List<AnswerData>): NightData =
        night.copy(questions = listOf(night.questions[0].copy(answers = answers)) + night.questions.drop(1))
}
