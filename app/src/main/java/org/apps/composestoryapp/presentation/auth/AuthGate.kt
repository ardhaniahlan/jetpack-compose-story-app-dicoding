package org.apps.composestoryapp.presentation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import org.apps.composestoryapp.presentation.splash.SplashScreen
import org.apps.composestoryapp.presentation.splash.SplashState
import org.apps.composestoryapp.remote.SessionManager

@Composable
fun AuthGate(
    sessionManager: SessionManager,
    navController: NavHostController
) {
    val tokenState = produceState<SplashState>(initialValue = SplashState.Loading) {
        delay(1000)

        sessionManager.sessionFlow.collect { session ->
            value = if (session?.token.isNullOrEmpty()) {
                SplashState.Unauthenticated
            } else {
                SplashState.Authenticated
            }
        }
    }

    when (tokenState.value) {
        SplashState.Loading -> SplashScreen()
        SplashState.Unauthenticated -> {
            LaunchedEffect(Unit) {
                navController.navigate("login") {
                    popUpTo(0)
                }
            }
        }
        SplashState.Authenticated -> {
            LaunchedEffect(Unit) {
                navController.navigate("home") {
                    popUpTo(0)
                }
            }
        }
    }
}

