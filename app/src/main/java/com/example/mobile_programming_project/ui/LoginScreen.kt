package com.example.mobile_programming_project.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
                )
            )
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Gather",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Sign in",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                enabled = !isLoading
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                enabled = !isLoading
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    context,
                                    "Signed in as ${task.result?.user?.email}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                onLoginSuccess()
                            } else {
                                val exception = task.exception
                                Log.e("LoginError", "Login failed: ${exception?.message}", exception)

                                errorMessage = when {
                                    exception?.message?.contains("network", ignoreCase = true) == true ->
                                        "Network error. Check your internet connection."
                                    exception?.message?.contains("password", ignoreCase = true) == true ->
                                        "Invalid email or password"
                                    exception?.message?.contains("user", ignoreCase = true) == true ||
                                            exception?.message?.contains("record", ignoreCase = true) == true ->
                                        "User not found. Please check your email."
                                    exception?.message?.contains("email", ignoreCase = true) == true ->
                                        "Invalid email format"
                                    else -> exception?.message ?: "Login failed. Please try again."
                                }
                            }
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(50)),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                Text(if (isLoading) "Logging in..." else "Sign in", fontSize = 18.sp)
            }

            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color(0xFFFFCDD2),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text("Don't have an account?", color = Color.White)
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = { /* TODO: Navigate to Sign Up */ }) {
                    Text("Sign up", color = Color.White)
                }
            }
        }
    }
}