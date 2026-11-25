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
    private val explicitSettings: Settings? = null,
) : SaveStorage {
    private val settings: Settings? by lazy {
        try {
            explicitSettings ?: Settings()
        } catch (e: Throwable) {
            null
        }
    }
    private val fallbackStorage = InMemorySaveStorage()

    override fun read(): String? {
        return try {
            settings?.getStringOrNull(key) ?: fallbackStorage.read()
        } catch (e: Throwable) {
            fallbackStorage.read()
        }
    }

    override fun write(value: String) {
        try {
            settings?.putString(key, value)
        } catch (e: Throwable) {
            // ignore
        }
        fallbackStorage.write(value)
    }

    override fun clear() {
        try {
            settings?.remove(key)
        } catch (e: Throwable) {
            // ignore
        }
        fallbackStorage.clear()
    }
}

/** Para pruebas. */
class InMemorySaveStorage : SaveStorage {
    private var value: String? = null
    override fun read(): String? = value
    override fun write(value: String) { this.value = value }
    override fun clear() { value = null }
}
