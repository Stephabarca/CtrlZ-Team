package com.example.videojuego.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.videojuego.history.formatEffect
import com.example.videojuego.history.formatSeconds
import videojuego.shared.generated.resources.*
import com.example.videojuego.story.model.Opcion
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

private const val DEMO_LIMIT_SECONDS = 8

// TEMPORAL: datos de prueba. Los textos reales vendrán del JSON de la historia.
private val DEMO_QUESTIONS = listOf(
    DemoQuestion(
        id = "q1",
        text = "Mara cuenta que tuvo un mal día. ¿Cómo reaccionas?",
        options = listOf(
            Opcion("q1_a", "La escucho y le pregunto cómo se siente", "demo", mapOf("mara" to 5)),
            Opcion("q1_b", "Le digo que no es para tanto", "demo", mapOf("mara" to -3)),
            Opcion("q1_c", "Cambio de tema", "demo", mapOf("mara" to -1)),
        ),
    ),
    DemoQuestion(
        id = "q2",
        text = "Leo prefiere pasar la tarde solo. ¿Qué haces?",
        options = listOf(
            Opcion("q2_a", "Respeto su espacio y le propongo vernos luego", "demo", mapOf("leo" to 4)),
            Opcion("q2_b", "Insisto hasta que me acompañe", "demo", mapOf("leo" to -4)),
        ),
    ),
    DemoQuestion(
        id = "q3",
        text = "Hay un desacuerdo sobre cómo repartir la comida.",
        options = listOf(
            Opcion("q3_a", "Propongo que decidamos entre todos", "demo", mapOf("mara" to 2, "leo" to 2)),
            Opcion("q3_b", "Decido yo porque tengo razón", "demo", mapOf("mara" to -2, "leo" to -2)),
        ),
    ),
)

/** Pantalla TEMPORAL para probar el historial de respuestas. Se elimina con IP-11. */
@Composable
fun AnswerHistoryDemoScreen(onBack: () -> Unit) {
    val viewModel = viewModel {
        AnswerRoundDemoViewModel(DEMO_QUESTIONS, DEMO_LIMIT_SECONDS.seconds)
    }
    val index by viewModel.index.collectAsState()
    val records by viewModel.history.records.collectAsState()

    val question = viewModel.questions.getOrNull(index)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.answers_demo_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        if (question != null) {
            Text(
                text = stringResource(
                    Res.string.question_progress,
                    index + 1,
                    viewModel.questions.size,
                ),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(text = question.text, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(Res.string.answers_time_limit, DEMO_LIMIT_SECONDS),
                style = MaterialTheme.typography.labelMedium,
            )
            question.options.forEach { option ->
                Button(
                    onClick = { viewModel.answer(option) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(option.texto) }
            }
            Button(onClick = { viewModel.simulateTimeout() }) {
                Text(stringResource(Res.string.answers_simulate_timeout))
            }
        } else {
            Text(
                text = stringResource(Res.string.round_finished),
                style = MaterialTheme.typography.titleLarge,
            )
            Button(onClick = { viewModel.restart() }) {
                Text(stringResource(Res.string.round_restart))
            }
        }

        Text(
            text = stringResource(Res.string.history_title),
            style = MaterialTheme.typography.titleMedium,
        )
        if (records.isEmpty()) {
            Text(stringResource(Res.string.history_empty))
        }
        records.forEach { record ->
            val time = formatSeconds(record.timeSpentMillis)
            Text(
                text = if (record.isNoAnswer) {
                    stringResource(Res.string.history_row_no_answer, record.questionId, time)
                } else {
                    stringResource(
                        Res.string.history_row_answered,
                        record.questionId,
                        record.optionId.orEmpty(),
                        time,
                        formatEffect(record.effect),
                    )
                },
            )
        }

        Button(onClick = onBack) { Text(stringResource(Res.string.action_back)) }
    }
}
