@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.mobile_programming_project.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Post(
    val id: String,
    val userName: String,
    val timeAgo: String,
    val category: String,
    val content: String,
    val likes: Int,
    val comments: Int
)

private val demoPosts = listOf(
    Post("1", "Edein Vindain", "5 min ago", "Lost & Found", "Lost my water bottle. It looks like this.", 999, 320),
    Post("2", "Dian Cinne", "10 min ago", "Marketplace", "Selling a used backpack.", 10000, 300),
    Post("3", "John Put", "23 min ago", "Safety", "Someone stole my backpack near the marketplace.", 140, 19)
)

@Composable
fun HomeScreen(onSignOut: () -> Unit = {}) {
    val gradient = Brush.verticalGradient(
        listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "HOME",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFEAD6FF)
                    )
                )
            },
            actions = {
                Button(onClick = onSignOut) { Text("Sign out") }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            )
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(demoPosts) { post ->
                PostCard(post)
            }
        }
    }
}

@Composable
private fun PostCard(post: Post) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2458B6).copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${post.userName} • ${post.timeAgo}", color = Color(0xFFEFF3FF),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(post.category, color = Color(0xFFEAD6FF),
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(post.content, color = Color.White)
            Spacer(Modifier.height(12.dp))
            RowActions(likes = post.likes, comments = post.comments)
        }
    }
}

@Composable
private fun RowActions(likes: Int, comments: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Like", tint = Color.White)
        }
        Text("$likes", color = Color.White)

        Spacer(Modifier.weight(1f))

        IconButton(onClick = { }) {
            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments", tint = Color.White)
        }
        Text("$comments", color = Color.White)

        Spacer(Modifier.weight(1f))

        IconButton(onClick = { }) {
            Icon(Icons.Outlined.Send, contentDescription = "Share", tint = Color.White)
        }
    }
}
