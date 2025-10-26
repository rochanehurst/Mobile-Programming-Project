package com.example.mobile_programming_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobile_programming_project.ui.LoginScreen
import com.example.mobile_programming_project.ui.theme.MobileProgrammingProjectTheme
import com.google.firebase.auth.FirebaseAuth
import com.example.mobile_programming_project.ui.HomeScreen
import com.example.mobile_programming_project.ui.SafetyReportScreen

class MainActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MobileProgrammingProjectTheme {
                AppNav(auth = auth)
            }
        }
    }
}

@Composable
private fun AppNav(auth: FirebaseAuth) {
    val nav = rememberNavController()
    val startDestination = if (auth.currentUser == null) "login" else "home"

    NavHost(navController = nav, startDestination = startDestination) {
        composable("login") {
            LoginScreen(auth = auth) {
                // When login succeeds → go to home and clear login from back stack
                nav.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("home") {
            HomeScreen(
                onSignOut = {
                    auth.signOut()
                    nav.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                onSafetyClick = { nav.navigate("safety_report") }
            )
        }


        composable("safety_report") {
            SafetyReportScreen(onBack = { nav.popBackStack() })
        }

    }
}
