package com.example.mobile_programming_project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mobile_programming_project.ui.LoginScreen
import com.example.mobile_programming_project.ui.theme.MobileProgrammingProjectTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()

        setContent {
            MobileProgrammingProjectTheme {
                LoginScreen(
                    auth = auth,
                    onLoginSuccess = {
                        // TODO: Navigate to your next screen after successful login
                    }
                )
            }
        }
    }
}
