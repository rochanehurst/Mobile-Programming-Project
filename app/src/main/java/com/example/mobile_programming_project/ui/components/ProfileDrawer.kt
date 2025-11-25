package com.example.mobile_programming_project.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ProfileMenuItem(
    val icon: ImageVector,
    val title: String,
    val onClick: () -> Unit
)

@Composable
fun ProfileDrawerContent(
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = Color(0xFF1565C0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1565C0), Color(0xFF1976D2))
                    )
                )
        ) {
            // Profile Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(60.dp),
                        tint = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Student Name",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "student@university.edu",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Divider(
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Menu Items
            val menuItems = listOf(
                ProfileMenuItem(
                    icon = Icons.Outlined.Person,
                    title = "My Profile"
                ) { /* Navigate to profile */ },
                ProfileMenuItem(
                    icon = Icons.Outlined.FavoriteBorder,
                    title = "Saved Posts"
                ) { /* Navigate to saved */ },
                ProfileMenuItem(
                    icon = Icons.Outlined.ShoppingBag,
                    title = "My Listings"
                ) { /* Navigate to listings */ },
                ProfileMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = "Settings"
                ) { /* Navigate to settings */ },
                ProfileMenuItem(
                    icon = Icons.Outlined.Info,
                    title = "About & Help"
                ) { /* Navigate to about */ }
            )

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(menuItems) { item ->
                    DrawerMenuItem(
                        icon = item.icon,
                        title = item.title,
                        onClick = {
                            item.onClick()
                            onCloseDrawer()
                        }
                    )
                }
            }

            // Sign Out Button
            Divider(
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            DrawerMenuItem(
                icon = Icons.Outlined.ExitToApp,
                title = "Sign Out",
                onClick = onSignOut,
                textColor = Color(0xFFFFCDD2)
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
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
            contentDescription = title,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}
