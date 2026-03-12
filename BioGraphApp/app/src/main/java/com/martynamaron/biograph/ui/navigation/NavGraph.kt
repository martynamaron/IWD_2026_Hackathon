package com.martynamaron.biograph.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.martynamaron.biograph.BioGraphApplication
import com.martynamaron.biograph.ui.screens.calendar.CalendarScreen
import com.martynamaron.biograph.ui.screens.datatype.DataTypeListScreen
import com.martynamaron.biograph.ui.screens.onboarding.OnboardingScreen
import com.martynamaron.biograph.ui.screens.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable object OnboardingRoute
@Serializable object CalendarRoute
@Serializable object DataTypeListRoute
@Serializable object SettingsRoute

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as BioGraphApplication

    // One-time check: determine start destination based on whether data types exist
    var startRoute by remember { mutableStateOf<Any?>(null) }
    LaunchedEffect(Unit) {
        val count = app.dataTypeRepository.getCount()
        startRoute = if (count == 0) OnboardingRoute else CalendarRoute
    }

    val startDestination = startRoute ?: return

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(CalendarRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )
        }
        composable<CalendarRoute> {
            CalendarScreen(
                onNavigateToDataTypes = { navController.navigate(DataTypeListRoute) },
                onNavigateToSettings = { navController.navigate(SettingsRoute) }
            )
        }
        composable<DataTypeListRoute> {
            DataTypeListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
