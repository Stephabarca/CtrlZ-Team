package com.example.videojuego.data.content

import com.example.videojuego.data.GameJson
import kotlinx.serialization.json.Json

object ContentFiles {
    const val CONFIG = "config.json"
    const val MOTIVES = "motives.json"
    const val CHARACTERS = "characters.json"
    const val NIGHT_1 = "night1.json"
    const val NIGHT_2 = "night2.json"
    const val NIGHT_3 = "night3.json"
    const val MINIGAMES = "minigames.json"
    const val ENDINGS = "endings.json"

    val ALL = listOf(CONFIG, MOTIVES, CHARACTERS, NIGHT_1, NIGHT_2, NIGHT_3, MINIGAMES, ENDINGS)
}

/** Convierte los textos JSON en [GameContent]. No depende de recursos, así que se prueba en commonTest. */
object ContentDecoder {
    fun decode(files: Map<String, String>, json: Json = GameJson): GameContent {
        fun text(name: String): String = files[name] ?: error("Falta el archivo de contenido: $name")
        return GameContent(
            config = json.decodeFromString<GameConfig>(text(ContentFiles.CONFIG)),
            motives = json.decodeFromString<List<MotiveData>>(text(ContentFiles.MOTIVES)),
            characters = json.decodeFromString<List<CharacterData>>(text(ContentFiles.CHARACTERS)),
            nights = listOf(ContentFiles.NIGHT_1, ContentFiles.NIGHT_2, ContentFiles.NIGHT_3)
                .map { json.decodeFromString<NightData>(text(it)) }
                .sortedBy { it.order },
            minigames = json.decodeFromString<List<MinigameData>>(text(ContentFiles.MINIGAMES)),
            endings = json.decodeFromString<List<EndingData>>(text(ContentFiles.ENDINGS)),
        )
    }
}

/**
 * Lee los JSON de composeResources/files/. [readFile] recibe rutas como "files/config.json".
 * En la app se usa con `Res.readBytes` (ver GameServices).
 */
class ContentRepository(private val readFile: suspend (String) -> ByteArray) {
    suspend fun load(): GameContent {
        val files = ContentFiles.ALL.associateWith { readFile("files/$it").decodeToString() }
        return ContentDecoder.decode(files)
    }
}
