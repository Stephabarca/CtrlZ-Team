package com.example.videojuego.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.videojuego.data.save.GameSettings
import com.example.videojuego.settings.SettingsLogic
import com.example.videojuego.ui.components.ActionButton
import com.example.videojuego.ui.theme.IslaColors
import com.example.videojuego.ui.theme.PixelFontFamily

/**
 * Ajustes: velocidad del texto y volumen de música y efectos.
 * [onChange] se llama en cada cambio con los valores ya acotados; ahí se guardan.
 */
@Composable
fun SettingsScreen(
    initial: GameSettings,
    onChange: (GameSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var settings by remember { mutableStateOf(initial.sanitized()) }
    fun update(new: GameSettings) {
        settings = new
        onChange(new)
    }

    Box(modifier.fillMaxSize().background(IslaColors.Primary).safeDrawingPadding()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(SettingsStrings.Title, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .border(3.dp, IslaColors.Ink, RectangleShape)
                    .background(IslaColors.Paper, RectangleShape)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SettingSlider(
                    SettingsStrings.TextSpeed,
                    SettingsLogic.speedFromDelay(settings.textDelayMs),
                ) { update(SettingsLogic.withTextSpeed(settings, it)) }
                SettingSlider(SettingsStrings.Music, settings.musicVolume) { update(SettingsLogic.withMusic(settings, it)) }
                SettingSlider(SettingsStrings.Sfx, settings.sfxVolume) { update(SettingsLogic.withSfx(settings, it)) }
            }
            Text(SettingsStrings.AutoSaved, color = IslaColors.OnDark, fontFamily = PixelFontFamily, fontSize = 12.sp)
            ActionButton(text = SettingsStrings.Back, onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
private fun SettingSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = IslaColors.Ink, fontFamily = PixelFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text("${SettingsLogic.percent(value)} %", color = IslaColors.Ink, fontFamily = PixelFontFamily, fontSize = 13.sp)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = IslaColors.Action,
                activeTrackColor = IslaColors.Action,
                inactiveTrackColor = Color(0xFF7C7396),
            ),
        )
    }
}
