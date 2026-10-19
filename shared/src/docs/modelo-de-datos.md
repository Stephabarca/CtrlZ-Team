# Modelo de datos de Isla Perdida

El juego funciona 100 % sin conexión, por lo que no usa servidor. Los datos se dividen en dos bloques:

| Bloque | Qué es | Dónde vive | ¿Cambia al jugar? |
|---|---|---|---|
| **Contenido** | Personajes, noches, preguntas, respuestas, minijuegos y finales | Archivos JSON en `shared/src/commonMain/composeResources/files/` | No (solo lectura) |
| **Progreso** | Partida guardada y ajustes del jugador | Almacenamiento local del dispositivo (JSON) | Sí |

Ambos bloques se conectan por los mismos identificadores (`characterId`, `questionId`, `answerId`, `minigameId`, `endingId`). El modelo relacional completo está en [`schema.sql`](schema.sql).

## Diagrama entidad-relación

```mermaid
erDiagram
    CHARACTER ||--o{ NIGHT : "protagoniza"
    NIGHT ||--o{ DIALOGUE_LINE : "tiene"
    NIGHT ||--|{ QUESTION : "contiene"
    QUESTION ||--|{ ANSWER : "ofrece"
    MOTIVE ||--o{ ANSWER : "clasifica"
    MINIGAME ||--o{ NIGHT : "se juega en"
    CHARACTER ||--o{ MINIGAME : "participa"
    AFFINITY_STAGE }o--|| GAME_CONFIG : "define"

    SAVE_GAME ||--|{ CHARACTER_AFFINITY : "registra"
    SAVE_GAME ||--o{ PLAYER_ANSWER : "guarda"
    SAVE_GAME ||--o{ MINIGAME_RESULT : "guarda"
    SAVE_GAME ||--o{ GAME_FLAG : "marca"
    SAVE_GAME }o--o| ENDING : "termina en"
    CHARACTER ||--o{ CHARACTER_AFFINITY : "recibe"
    QUESTION ||--o{ PLAYER_ANSWER : "responde"
    ANSWER ||--o{ PLAYER_ANSWER : "elige"
    MINIGAME ||--o{ MINIGAME_RESULT : "produce"

    CHARACTER {
        string id PK
        string name
        int age
        string occupation
        string accent_color
        string difficulty
        string learning
        string summary
        string pending_learning
    }
    NIGHT {
        string id PK
        int night_order
        string character_id FK
        string minigame_id FK
        string title
        string background_id
        int time_limit_sec
        float answer_weight
    }
    DIALOGUE_LINE {
        int id PK
        string night_id FK
        string section "intro u outro"
        int position
        string speaker_id
        string text
        string expression
    }
    QUESTION {
        string id PK
        string night_id FK
        int position
        string speaker_id
        string expression
        string prompt
    }
    ANSWER {
        string id PK
        string question_id FK
        string motive_id FK
        string text
        int affinity_delta
        string feedback
    }
    MOTIVE {
        string id PK
        string label
        boolean healthy
    }
    MINIGAME {
        string id PK
        string character_id FK
        string name
        string type
        int max_affinity
        int duration_sec
        string difficulty
    }
    ENDING {
        string id PK
        string title
        string text
        int priority
        int min_characters_above
        int threshold
        int no_one_below
    }
    AFFINITY_STAGE {
        string id PK
        string name
        int min_value
    }
    GAME_CONFIG {
        int start_affinity
        int min_affinity
        int max_affinity
        int timeout_delta
    }
    SAVE_GAME {
        int id PK
        int version
        string phase
        boolean skipped_tutorial
        string ending_id FK
    }
    CHARACTER_AFFINITY {
        int save_id FK
        string character_id FK
        int value
    }
    PLAYER_ANSWER {
        int id PK
        int save_id FK
        string question_id FK
        string answer_id FK "nulo si se agotó el tiempo"
        long time_spent_ms
        boolean timed_out
        int affinity_delta
    }
    MINIGAME_RESULT {
        int id PK
        int save_id FK
        string minigame_id FK
        int score
        int affinity_granted
    }
    GAME_FLAG {
        int save_id FK
        string key
        boolean value
    }
```

## Contenido incluido

- **3 personajes:** Valentina Oliva (dificultad moderada), Lucas Ámbar (intermedia) y Lila Ariza (elevada), a partir de la biblia de personajes.
- **3 noches:** una por personaje, cada una con introducción, 5 preguntas con 4 respuestas, un minijuego y cierre.
- **15 preguntas y 60 respuestas**, cada una con su efecto en la afinidad, el motivo que demuestra y una retroalimentación explicativa.
- **27 motivos:** 11 saludables (escucha, empatía, asertividad...) y 16 de riesgo (celos, control, minimizar, chantaje...).
- **3 minijuegos** y **3 finales** (más reglas de afinidad por etapas).

### Dificultad por noche (IP-14)

| Noche | Personaje | Tiempo por pregunta | Peso de las respuestas |
|---|---|---|---|
| 1 | Valentina | 20 s | 1.0 |
| 2 | Lucas | 15 s | 1.0 |
| 3 | Lila | 12 s | 1.2 |

### Reglas de afinidad

- Valor de 0 a 100. Cada personaje empieza en 30.
- Efecto de una respuesta = `affinityDelta × answerWeight`, redondeado y acotado a 0–100.
- Si se agota el tiempo se aplica `timeoutDelta` (-2) y la respuesta se registra como "sin respuesta".
- Un minijuego otorga afinidad proporcional a su puntaje, hasta `maxAffinity` (10 a 15). **Un minijuego por sí solo nunca llega a la afinidad necesaria para un final** (30 + 15 = 45 como máximo).
- Etapas: Desconocidos (0), Conocidos (25), Amistad (50), Cercanía (75).

### Finales (se evalúan por prioridad)

| Final | Condición |
|---|---|
| Rescatados juntos | 2 personajes o más con afinidad de 70 o más y ninguno por debajo de 40 |
| Un vínculo profundo | 1 personaje o más con afinidad de 75 o más |
| Cada quien su camino | Por defecto (siempre se cumple) |

## Checklist de relaciones sanas integrado (IP-28)

El validador de contenido (`ContentValidator`) hace cumplir el checklist de forma automática:

- Una respuesta cuyo motivo es de **riesgo** (celos, control, humillación, presión, chantaje...) **nunca puede sumar afinidad**.
- Una respuesta cuyo motivo es **saludable** nunca puede restarla.
- Los minijuegos no pueden otorgar más de 20 puntos.

Así, aunque alguien edite los JSON, el juego avisa si una escena premia una conducta tóxica.

## Cómo se relacionan contenido y progreso

1. El juego carga el contenido (`ContentRepository`) y lo valida (`ContentValidator`).
2. `ProgressRules.newGame` crea una partida con la afinidad inicial de cada personaje.
3. Cada respuesta pasa por `ProgressRules.recordAnswer` y se guarda en `SaveGame.answers`.
4. Cada minijuego pasa por `ProgressRules.recordMinigame`.
5. Al terminar la Noche 3, `ProgressRules.finish` elige el final.
6. `SaveRepository` guarda y restaura todo el `SaveGame` (IP-36).
