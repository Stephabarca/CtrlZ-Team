package com.example.videojuego.data

import com.example.videojuego.data.content.ContentRepository
import com.example.videojuego.data.save.SaveRepository
import com.example.videojuego.data.save.SettingsRepository
import com.example.videojuego.data.save.SettingsSaveStorage
import com.example.videojuego.data.save.StorageKeys
import videojuego.shared.generated.resources.Res

/** Punto único de acceso a los datos. Se reemplazará por Koin cuando se haga IP-2. */
object GameServices {
    val content: ContentRepository = ContentRepository { path -> Res.readBytes(path) }
    val saves: SaveRepository by lazy { SaveRepository(SettingsSaveStorage(StorageKeys.SAVE)) }
    val settings: SettingsRepository by lazy { SettingsRepository(SettingsSaveStorage(StorageKeys.SETTINGS)) }
}
