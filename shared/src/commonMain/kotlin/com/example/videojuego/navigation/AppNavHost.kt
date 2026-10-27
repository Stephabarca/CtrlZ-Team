package com.example.videojuego.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import videojuego.shared.generated.resources.*
import com.example.videojuego.ui.AnswerHistoryDemoScreen
import com.example.videojuego.ui.GamePlaceholderScreen
import com.example.videojuego.ui.MenuScreen
import com.example.videojuego.ui.SettingsPlaceholderScreen
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Grafo de navegación: Menú -> Intro -> Noche 1 -> Noche 2 -> Noche 3. */
@Composable
fun AppNavHost(onExit: () -> Unit) {
    val navController = rememberNavController()

    fun goToMenu() {
        navController.popBackStack(Screen.Menu.route, inclusive = false)
    }

    NavHost(navController = navController, startDestination = Screen.Menu.route) {
        composable(Screen.Menu.route) {
            MenuScreen(
                onPlay = { navController.navigate(GameFlow.first().route) },
                onContinue = { /* Se conectará con el guardado (EP-9) */ },
                onSettings = { navController.navigate(Screen.Settings.route) },
                onAnswersDemo = { navController.navigate(Screen.AnswersDemo.route) },
                onExit = onExit,
            )
        }

        GameFlow.sequence.forEach { screen ->
            composable(screen.route) {
                val next = GameFlow.next(screen)
                GamePlaceholderScreen(
                    title = stringResource(screen.titleRes()),
                    isLast = next == null,
                    onNext = {
                        if (next != null) navController.navigate(next.route) else goToMenu()
                    },
                    onBackConfirmed = ::goToMenu,
                )
            }
        }

        composable(Screen.Settings.route) {
            SettingsPlaceholderScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AnswersDemo.route) {
            AnswerHistoryDemoScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun Screen.titleRes(): StringResource = when (this) {
    Screen.Menu -> Res.string.menu_title
    Screen.Intro -> Res.string.screen_intro
    Screen.Night1 -> Res.string.screen_night1
    Screen.Night2 -> Res.string.screen_night2
    Screen.Night3 -> Res.string.screen_night3
    Screen.Settings -> Res.string.screen_settings
    Screen.AnswersDemo -> Res.string.answers_demo_title
}
