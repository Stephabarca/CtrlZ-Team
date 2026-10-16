package com.example.videojuego.data.save

import com.example.videojuego.data.GameJson
import kotlinx.serialization.json.Json

class SaveRepository(
    private val storage: SaveStorage,
    private val json: Json = GameJson,
) {
    fun save(game: SaveGame) = storage.write(json.encodeToString(SaveGame.serializer(), game))

    /** Distingue "no hay partida" de "partida dañada" para ofrecer iniciar una nueva (IP-36). */
    fun load(): LoadResult {
        val raw = storage.read() ?: return LoadResult.NoSave
        return try {
            val game = json.decodeFromString(SaveGame.serializer(), raw)
            if (game.version == SaveGame.CURRENT_VERSION) LoadResult.Loaded(game) else LoadResult.Corrupt
        } catch (e: IllegalArgumentException) {
            // SerializationException hereda de IllegalArgumentException
            LoadResult.Corrupt
        }
    }

    fun hasSave(): Boolean = load() is LoadResult.Loaded

    fun clear() = storage.clear()
}

class SettingsRepository(
    private val storage: SaveStorage,
    private val json: Json = GameJson,
) {
    fun load(): GameSettings {
        val raw = storage.read() ?: return GameSettings()
        return try {
            json.decodeFromString(GameSettings.serializer(), raw).sanitized()
        } catch (e: IllegalArgumentException) {
            GameSettings()
        }
    }

    fun save(settings: GameSettings) =
        storage.write(json.encodeToString(GameSettings.serializer(), settings.sanitized()))
}
