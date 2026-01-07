package com.example.sudoky.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sudoky.ui.screens.HomeScreen
import com.example.sudoky.ui.screens.GameScreen
import com.example.sudoky.ui.screens.WinScreen
import com.example.sudoky.ui.screens.LoseScreen
import com.example.sudoky.ui.screens.SplashScreen
import com.example.sudoky.sudoku.Difficulty

sealed class Route(val r: String) {
    data object Splash : Route("splash")
    data object Home : Route("home")
    data object Game : Route("game/{difficulty}") {
        fun build(diff: Difficulty): String = "game/${diff.name}"
    }
    data object Win : Route("win/{difficulty}/{elapsedSeconds}") {
        fun build(diff: Difficulty, elapsedSeconds: Int): String = "win/${diff.name}/${elapsedSeconds}"
    }
    data object Lose : Route("lose/{difficulty}/{elapsedSeconds}") {
        fun build(diff: Difficulty, elapsedSeconds: Int): String = "lose/${diff.name}/${elapsedSeconds}"
    }
}

private const val ARG_DIFFICULTY = "difficulty"
private const val ARG_ELAPSED = "elapsedSeconds"

@Composable
fun AppNav(
    isDarkTheme: Boolean,
    onThemeToggle: (Boolean) -> Unit
) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Route.Splash.r) {
        composable(Route.Splash.r) {
            SplashScreen(
                isDarkTheme = isDarkTheme,
                onDone = {
                    nav.navigate(Route.Home.r) {
                        popUpTo(Route.Splash.r) { inclusive = true }
                    }
                }
            )
        }

        composable(Route.Home.r) {
            HomeScreen(
                isDarkTheme = isDarkTheme,
                onToggleTheme = onThemeToggle,
                onNewGame = { diff ->
                    nav.navigate(Route.Game.build(diff))
                }
            )
        }

        composable(
            route = Route.Game.r,
            arguments = listOf(navArgument(ARG_DIFFICULTY) { type = NavType.StringType })
        ) { backStackEntry ->
            val diffName = backStackEntry.arguments?.getString(ARG_DIFFICULTY)
            val diff = diffName?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.NOVICE_9
            GameScreen(
                difficulty = diff,
                onBack = { nav.popBackStack() },
                onWin = { elapsedSeconds ->
                    nav.navigate(Route.Win.build(diff, elapsedSeconds)) {
                        popUpTo(Route.Game.r) { inclusive = true }
                    }
                },
                onLose = { elapsedSeconds ->
                    nav.navigate(Route.Lose.build(diff, elapsedSeconds)) {
                        popUpTo(Route.Game.r) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Route.Win.r,
            arguments = listOf(
                navArgument(ARG_DIFFICULTY) { type = NavType.StringType },
                navArgument(ARG_ELAPSED) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val diffName = backStackEntry.arguments?.getString(ARG_DIFFICULTY)
            val diff = diffName?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.NOVICE_9
            val elapsedSeconds = backStackEntry.arguments?.getInt(ARG_ELAPSED) ?: 0
            WinScreen(
                isDarkTheme = isDarkTheme,
                difficulty = diff,
                elapsedSeconds = elapsedSeconds,
                onMenu = {
                    nav.navigate(Route.Home.r) {
                        popUpTo(Route.Win.r) { inclusive = true }
                    }
                },
                onNewGame = { newDiff ->
                    nav.navigate(Route.Game.build(newDiff)) {
                        popUpTo(Route.Win.r) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Route.Lose.r,
            arguments = listOf(
                navArgument(ARG_DIFFICULTY) { type = NavType.StringType },
                navArgument(ARG_ELAPSED) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val diffName = backStackEntry.arguments?.getString(ARG_DIFFICULTY)
            val diff = diffName?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() } ?: Difficulty.NOVICE_9
            val elapsedSeconds = backStackEntry.arguments?.getInt(ARG_ELAPSED) ?: 0
            LoseScreen(
                isDarkTheme = isDarkTheme,
                difficulty = diff,
                elapsedSeconds = elapsedSeconds,
                onMenu = {
                    nav.navigate(Route.Home.r) {
                        popUpTo(Route.Lose.r) { inclusive = true }
                    }
                },
                onNewGame = { newDiff ->
                    nav.navigate(Route.Game.build(newDiff)) {
                        popUpTo(Route.Lose.r) { inclusive = true }
                    }
                }
            )
        }
    }
}
