package org.apps.composestoryapp.presentation.splash

sealed class SplashState {
    object Loading : SplashState()
    object Unauthenticated : SplashState()
    object Authenticated : SplashState()
}
