package com.example.runeboundmagic.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val SplashRoute = "splash"
private const val IntroRoute = "intro"

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
                IntroScreen()
            }
        }
    }
}

internal object Routes {
    const val Splash = SplashRoute
    const val Intro = IntroRoute
}
