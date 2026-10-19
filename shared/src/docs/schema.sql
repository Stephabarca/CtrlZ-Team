-- Modelo relacional de Isla Perdida (SQLite).
-- El contenido hoy vive en JSON y el progreso en un documento JSON; este esquema es el modelo
-- lógico equivalente y puede usarse tal cual con SQLDelight si se decide migrar a SQLite.

PRAGMA foreign_keys = ON;

-- ============ CONTENIDO (solo lectura) ============

CREATE TABLE game_config (
    id              INTEGER PRIMARY KEY CHECK (id = 1),
    start_affinity  INTEGER NOT NULL,
    min_affinity    INTEGER NOT NULL,
    max_affinity    INTEGER NOT NULL,
    timeout_delta   INTEGER NOT NULL,
    CHECK (min_affinity <= start_affinity AND start_affinity <= max_affinity)
);

CREATE TABLE affinity_stage (
    id         TEXT PRIMARY KEY,
    name       TEXT NOT NULL,
    min_value  INTEGER NOT NULL UNIQUE
);

CREATE TABLE character (
    id                TEXT PRIMARY KEY,
    name              TEXT NOT NULL,
    age               INTEGER NOT NULL,
    occupation        TEXT NOT NULL,
    accent_color      TEXT NOT NULL,
    difficulty        TEXT NOT NULL,
    learning          TEXT NOT NULL,
    summary           TEXT NOT NULL,
    pending_learning  TEXT NOT NULL
);

CREATE TABLE character_boundary (
    character_id  TEXT NOT NULL REFERENCES character(id),
    position      INTEGER NOT NULL,
    text          TEXT NOT NULL,
    PRIMARY KEY (character_id, position)
);

CREATE TABLE motive (
    id       TEXT PRIMARY KEY,
    label    TEXT NOT NULL,
    healthy  INTEGER NOT NULL CHECK (healthy IN (0, 1))
);

CREATE TABLE minigame (
    id            TEXT PRIMARY KEY,
    character_id  TEXT NOT NULL REFERENCES character(id),
    name          TEXT NOT NULL,
    type          TEXT NOT NULL,
    max_affinity  INTEGER NOT NULL CHECK (max_affinity BETWEEN 1 AND 20),
    duration_sec  INTEGER NOT NULL CHECK (duration_sec > 0),
    difficulty    TEXT NOT NULL
);

CREATE TABLE night (
    id              TEXT PRIMARY KEY,
    night_order     INTEGER NOT NULL UNIQUE,
    character_id    TEXT NOT NULL REFERENCES character(id),
    minigame_id     TEXT NOT NULL REFERENCES minigame(id),
    title           TEXT NOT NULL,
    background_id   TEXT NOT NULL,
    time_limit_sec  INTEGER NOT NULL CHECK (time_limit_sec > 0),
    answer_weight   REAL NOT NULL CHECK (answer_weight > 0)
);

CREATE TABLE dialogue_line (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    night_id    TEXT NOT NULL REFERENCES night(id),
    section     TEXT NOT NULL CHECK (section IN ('intro', 'outro')),
    position    INTEGER NOT NULL,
    speaker_id  TEXT NOT NULL,            -- 'narrator' o character.id
    text        TEXT NOT NULL,
    expression  TEXT NOT NULL DEFAULT 'neutral',
    UNIQUE (night_id, section, position)
);

CREATE TABLE question (
    id          TEXT PRIMARY KEY,
    night_id    TEXT NOT NULL REFERENCES night(id),
    position    INTEGER NOT NULL,
    speaker_id  TEXT NOT NULL,
    expression  TEXT NOT NULL DEFAULT 'neutral',
    prompt      TEXT NOT NULL,
    UNIQUE (night_id, position)
);

CREATE TABLE answer (
    id              TEXT PRIMARY KEY,
    question_id     TEXT NOT NULL REFERENCES question(id),
    motive_id       TEXT NOT NULL REFERENCES motive(id),
    text            TEXT NOT NULL,
    affinity_delta  INTEGER NOT NULL CHECK (affinity_delta BETWEEN -15 AND 15),
    feedback        TEXT NOT NULL
);
-- Regla de relaciones sanas (IP-28): una conducta de riesgo no puede sumar afinidad.
-- Se hace cumplir con este disparador (equivale a ContentValidator).
CREATE TRIGGER answer_healthy_rule_insert BEFORE INSERT ON answer
WHEN (SELECT healthy FROM motive WHERE id = NEW.motive_id) = 0 AND NEW.affinity_delta > 0
BEGIN
    SELECT RAISE(ABORT, 'Una conducta de riesgo no puede sumar afinidad');
END;

CREATE TABLE ending (
    id                    TEXT PRIMARY KEY,
    title                 TEXT NOT NULL,
    text                  TEXT NOT NULL,
    priority              INTEGER NOT NULL UNIQUE,
    min_characters_above  INTEGER NOT NULL DEFAULT 0,
    threshold             INTEGER NOT NULL DEFAULT 0,
    no_one_below          INTEGER NOT NULL DEFAULT 0
);

-- ============ PROGRESO (cambia al jugar) ============

CREATE TABLE save_game (
    id                INTEGER PRIMARY KEY,           -- ranura de guardado
    version           INTEGER NOT NULL,
    phase             TEXT NOT NULL CHECK (phase IN ('INTRO','NIGHT_1','NIGHT_2','NIGHT_3','ENDING')),
    skipped_tutorial  INTEGER NOT NULL DEFAULT 0 CHECK (skipped_tutorial IN (0, 1)),
    ending_id         TEXT REFERENCES ending(id)
);

CREATE TABLE character_affinity (
    save_id       INTEGER NOT NULL REFERENCES save_game(id) ON DELETE CASCADE,
    character_id  TEXT NOT NULL REFERENCES character(id),
    value         INTEGER NOT NULL CHECK (value BETWEEN 0 AND 100),
    PRIMARY KEY (save_id, character_id)
);

CREATE TABLE player_answer (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    save_id        INTEGER NOT NULL REFERENCES save_game(id) ON DELETE CASCADE,
    question_id    TEXT NOT NULL REFERENCES question(id),
    answer_id      TEXT REFERENCES answer(id),       -- NULL si se agotó el tiempo
    time_spent_ms  INTEGER NOT NULL CHECK (time_spent_ms >= 0),
    timed_out      INTEGER NOT NULL CHECK (timed_out IN (0, 1)),
    affinity_delta INTEGER NOT NULL,
    CHECK ((timed_out = 1 AND answer_id IS NULL) OR (timed_out = 0 AND answer_id IS NOT NULL))
);

CREATE TABLE minigame_result (
    id                INTEGER PRIMARY KEY AUTOINCREMENT,
    save_id           INTEGER NOT NULL REFERENCES save_game(id) ON DELETE CASCADE,
    minigame_id       TEXT NOT NULL REFERENCES minigame(id),
    score             INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    affinity_granted  INTEGER NOT NULL
);

CREATE TABLE game_flag (
    save_id  INTEGER NOT NULL REFERENCES save_game(id) ON DELETE CASCADE,
    key      TEXT NOT NULL,
    value    INTEGER NOT NULL CHECK (value IN (0, 1)),
    PRIMARY KEY (save_id, key)
);

CREATE TABLE game_settings (
    id             INTEGER PRIMARY KEY CHECK (id = 1),
    text_delay_ms  INTEGER NOT NULL CHECK (text_delay_ms BETWEEN 10 AND 100),
    music_volume   REAL NOT NULL CHECK (music_volume BETWEEN 0 AND 1),
    sfx_volume     REAL NOT NULL CHECK (sfx_volume BETWEEN 0 AND 1)
);
