package com.example.mobile_programming_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileDrawerContent(
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()

    var userPostCount by remember { mutableStateOf(0) }
    var userLikesReceived by remember { mutableStateOf(0) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Fetch user stats - matches the actual logged-in user
    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { user ->
            try {
                // Get the actual email from Firebase Auth
                val userEmail = user.email ?: return@LaunchedEffect

                // Query posts where userName matches the current user's email prefix or full email
                val postsSnapshot = Firebase.firestore.collection("posts")
                    .get()
                    .await()

                // Filter posts that belong to this user
                val userPosts = postsSnapshot.documents.filter { doc ->
                    val postUserName = doc.getString("userName") ?: ""
                    val postEmail = doc.getString("userEmail") ?: ""

                    // Check if post belongs to current user by userName or email
                    postUserName.equals(userEmail.substringBefore("@"), ignoreCase = true) ||
                            postUserName.equals(userEmail, ignoreCase = true) ||
                            postEmail.equals(userEmail, ignoreCase = true)
                }

                userPostCount = userPosts.size
                userLikesReceived = userPosts.sumOf {
                    (it.getLong("likes") ?: 0).toInt()
                }
            } catch (e: Exception) {
                // Handle error silently
                userPostCount = 0
                userLikesReceived = 0
            }
        }
    }

    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    )

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section with Profile
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onCloseDrawer) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // User Name
                Text(
                    text = currentUser?.email?.substringBefore("@") ?: "User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Email
                Text(
                    text = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(label = "Posts", value = userPostCount.toString())
                    StatItem(label = "Likes", value = userLikesReceived.toString())
                }
            }

            Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)

            // Menu Items
            DrawerMenuItem(
                icon = Icons.Outlined.Person,
                text = "My Profile",
                onClick = {
                    // Navigate to profile screen
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Outlined.Article,
                text = "My Posts",
                onClick = {
                    // Navigate to my posts screen
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Outlined.Bookmark,
                text = "Saved Posts",
                onClick = {
                    // Navigate to saved posts
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Outlined.ChatBubbleOutline,
                text = "My Comments",
                onClick = {
                    // Navigate to my comments
                    onCloseDrawer()
                }
            )

            Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)

            DrawerMenuItem(
                icon = Icons.Outlined.Settings,
                text = "Settings",
                onClick = {
                    showSettingsDialog = true
                }
            )

            DrawerMenuItem(
                icon = Icons.Outlined.Help,
                text = "Help & Support",
                onClick = {
                    // Navigate to help
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Outlined.Info,
                text = "About",
                onClick = {
                    showAboutDialog = true
                }
            )

            DrawerMenuItem(
                icon = Icons.Outlined.Share,
                text = "Share App",
                onClick = {
                    // Share app functionality
                    onCloseDrawer()
                }
            )

            Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)

            DrawerMenuItem(
                icon = Icons.Outlined.ExitToApp,
                text = "Sign Out",
                onClick = onSignOut,
                textColor = Color(0xFFFFCDD2)
            )

            Spacer(Modifier.height(16.dp))

            // Version info at bottom
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Campus Connect") },
            text = {
                Column {
                    Text("Campus Connect v1.0.0")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A community platform for students to connect, share, " +
                                "and stay informed about campus life.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Settings") },
            text = {
                Column {
                    Text("Settings coming soon!")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "• Notification preferences\n" +
                                "• Privacy settings\n" +
                                "• Theme options\n" +
                                "• Account management",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}