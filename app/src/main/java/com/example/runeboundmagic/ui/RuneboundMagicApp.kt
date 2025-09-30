package com.example.runeboundmagic.ui

import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import com.example.runeboundmagic.CharacterSelectionActivity
import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.MainActivity
import com.example.runeboundmagic.codex.CodexScreen

private const val SplashRoute = "splash"
private const val IntroRoute = "intro"
private const val LobbyRoute = "lobby"
private const val CodexRoute = "codex"

private val SplashColorScheme = darkColorScheme(
    primary = Color(0xFF38B6FF),
    onPrimary = Color.Black,
    secondary = Color(0xFF00E5A0),
    onSecondary = Color.Black,
    background = Color(0xFF000814),
    onBackground = Color(0xFFE0F2F1)
)

@Composable
fun RuneboundMagicApp() {
    val navController = rememberNavController()
    MaterialTheme(colorScheme = SplashColorScheme) {
        NavHost(
            navController = navController,
            startDestination = SplashRoute
        ) {
            composable(SplashRoute) {
                SplashScreen(navController = navController)
            }
            composable(IntroRoute) {
                IntroScreen(
                    onIntroFinished = {
                        navController.navigate(Routes.Lobby) {
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    }
                )
            }
            composable(LobbyRoute) {
                val context = LocalContext.current
                LobbyScreen(
                    onBack = { navController.popBackStack() },
                    onSelectHero = {
                        context.startActivity(
                            Intent(context, CharacterSelectionActivity::class.java)
                        )
                    },
                    onStartBattle = { hero: HeroOption, _ ->
                        val intent = Intent(context, MainActivity::class.java).apply {
                            putExtra(MainActivity.EXTRA_SELECTED_HERO, hero.name)
                        }
                        context.startActivity(intent)
                    },
                    onOpenCodex = {
                        navController.navigate(Routes.Codex)
                    }
                )
            }
            composable(CodexRoute) {
                CodexScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

internal object Routes {
    const val Splash = SplashRoute
    const val Intro = IntroRoute
    const val Lobby = LobbyRoute
    const val Codex = CodexRoute
}
