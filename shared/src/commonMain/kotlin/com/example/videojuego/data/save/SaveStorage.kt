package com.example.videojuego.data.save

import com.russhwolf.settings.Settings

/** Almacén de texto por clave. Permite cambiar el medio (Settings, SQLite, archivo) sin tocar la lógica. */
interface SaveStorage {
    fun read(): String?
    fun write(value: String)
    fun clear()
}

object StorageKeys {
    const val SAVE = "save_slot_1"
    const val SETTINGS = "game_settings"
}

/** Guardado real: preferencias del sistema (Android, escritorio e iOS). */
class SettingsSaveStorage(
    private val key: String,
    private val settings: Settings = Settings(),
) : SaveStorage {
    override fun read(): String? = settings.getStringOrNull(key)
    override fun write(value: String) = settings.putString(key, value)
    override fun clear() = settings.remove(key)
}

/** Para pruebas. */
class InMemorySaveStorage : SaveStorage {
    private var value: String? = null
    override fun read(): String? = value
    override fun write(value: String) { this.value = value }
    override fun clear() { value = null }
}
