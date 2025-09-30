package com.example.runeboundmagic.ui

import android.net.Uri
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.runeboundmagic.HeroOption
import com.example.runeboundmagic.audio.BackgroundMusicController
import com.example.runeboundmagic.codex.CodexScreen

private const val SplashRoute = "splash"
private const val IntroRoute = "intro"
private const val LobbyRoute = "lobby"
private const val CodexRoute = "codex"
private const val Match3Route = "match3"

private val SplashColorScheme = darkColorScheme(
    primary = Color(0xFF38B6FF),
    onPrimary = Color.Black,
    secondary = Color(0xFF00E5A0),
    onSecondary = Color.Black,
    background = Color(0xFF000814),
    onBackground = Color(0xFFE0F2F1)
)

@Composable
fun RuneboundMagicApp(musicController: BackgroundMusicController) {
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
                    onIntroShown = { musicController.startOrResume() },
                    onIntroFinished = {
                        navController.navigate(Routes.Lobby) {
                            popUpTo(Routes.Splash) { inclusive = true }
                        }
                    }
                )
            }
            composable(LobbyRoute) {
                LobbyScreen(
                    onBack = { navController.popBackStack() },
                    onStartBattle = { hero: HeroOption, heroName ->
                        musicController.stop()
                        val encodedName = Uri.encode(heroName)
                        navController.navigate("${Routes.Match3}/${hero.name}/$encodedName")
                    },
                    onOpenCodex = {
                        navController.navigate(Routes.Codex)
                    },
                    onLobbyShown = { musicController.startOrResume() }
                )
            }
            composable("$Match3Route/{hero}/{player}") { backStackEntry ->
                val heroArg = backStackEntry.arguments?.getString("hero")
                val playerArg = backStackEntry.arguments?.getString("player") ?: ""
                val selectedHero = HeroOption.fromName(heroArg)
                val heroName = Uri.decode(playerArg)
                Match3Screen(
                    heroOption = selectedHero,
                    heroName = heroName,
                    onExitBattle = {
                        navController.popBackStack()
                        musicController.startOrResume()
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
    const val Match3 = Match3Route
}
