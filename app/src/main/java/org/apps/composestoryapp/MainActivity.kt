package org.apps.composestoryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dagger.hilt.android.AndroidEntryPoint
import org.apps.composestoryapp.presentation.auth.AuthGate
import org.apps.composestoryapp.presentation.auth.LoginScreen
import org.apps.composestoryapp.presentation.auth.RegisterScreen
import org.apps.composestoryapp.presentation.home.HomeScreen
import org.apps.composestoryapp.presentation.home.StoryDetailScreen
import org.apps.composestoryapp.presentation.profile.ProfileScreen
import org.apps.composestoryapp.remote.SessionManager
import org.apps.composestoryapp.ui.theme.ComposeStoryAppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val snackbarHostState = remember { SnackbarHostState() }

            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            val showBottomNav = currentRoute in listOf(
                BottomNavItem.Home.route,
                BottomNavItem.Profile.route
            )

            ComposeStoryAppTheme {
                Scaffold(
                    bottomBar = {
                        if (showBottomNav) {
                            BottomNavigationBar(navController)
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState
                        )
                    },
                    contentWindowInsets = WindowInsets.safeGestures
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        startDestination = "auth_gate"
                    ) {
                        // Auth
                        composable("auth_gate") {
                            AuthGate(
                                sessionManager = sessionManager,
                                navController = navController
                            )
                        }
                        composable("login") {
                            LoginScreen(navController)
                        }
                        composable("register") {
                            RegisterScreen(navController)
                        }

                        // App
                        composable("home"){
                            HomeScreen(navController)
                        }
                        composable("profile"){
                            ProfileScreen(navController)
                        }

                        composable(
                            route = "storydetail/{storyId}",
                            arguments = listOf(
                                navArgument("storyId") { type = NavType.StringType }
                            )
                        ){ backStackEntry ->
                            val storyId = backStackEntry.arguments?.getString("storyId") ?: ""
                            StoryDetailScreen(
                                storyId = storyId,
                                onBackClick = { navController.popBackStack() }
                            )
                        }

                    }
                }
            }
        }
    }
}