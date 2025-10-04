@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.mobile_programming_project.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// ---------------------- DATA ----------------------
data class Post(
    val id: String,
    val userName: String,
    val timeAgo: String,
    val category: String,
    val content: String,
    val likes: Int,
    val comments: Int,
    val imageUri: Uri? = null
)

val categories = listOf("Home", "Lost & Found", "Marketplace", "Safety")

// ---------------------- HOME SCREEN ----------------------
@Composable
fun HomeScreen(onSignOut: () -> Unit = {}) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5)))
    var selectedCategory by remember { mutableStateOf("Home") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showCreatePost by remember { mutableStateOf(false) }
    val posts = remember { mutableStateListOf<Post>() }

    // 🔥 Load Firestore posts on first render
    LaunchedEffect(Unit) {
        val snapshot = Firebase.firestore.collection("posts").get().await()
        val loaded = snapshot.documents.map { doc ->
            Post(
                id = doc.id,
                userName = doc.getString("userName") ?: "Unknown",
                timeAgo = doc.getString("timeAgo") ?: "",
                category = doc.getString("category") ?: "Home",
                content = doc.getString("content") ?: "",
                likes = (doc.getLong("likes") ?: 0).toInt(),
                comments = (doc.getLong("comments") ?: 0).toInt(),
                imageUri = doc.getString("imageUrl")?.let { Uri.parse(it) }
            )
        }
        posts.clear()
        posts.addAll(loaded)
    }

    val filteredPosts = if (selectedCategory == "Home") posts
    else posts.filter { it.category.equals(selectedCategory, ignoreCase = true) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePost = true },
                containerColor = Color(0xFF5E35B1),
                contentColor = Color.White
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(paddingValues)
        ) {
            TopAppBar(
                navigationIcon = {
                    if (selectedCategory != "Home") {
                        IconButton(onClick = { selectedCategory = "Home" }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back to Home", tint = Color.White)
                        }
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = selectedCategory.uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFEAD6FF)
                            )
                        )
                        IconButton(onClick = { dropdownExpanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select category", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                actions = {
                    TextButton(onClick = onSignOut) { Text("Sign out", color = Color.White) }
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
                items(filteredPosts) { post ->
                    PostCard(post = post, onCategoryClick = { clickedCategory ->
                        selectedCategory = clickedCategory
                    })
                }
            }
        }

        if (showCreatePost) {
            CreatePostDialog(
                onDismiss = { showCreatePost = false },
                onPostCreated = { newPost ->
                    posts.add(0, newPost)
                    showCreatePost = false
                }
            )
        }
    }
}

// ---------------------- POST CARD ----------------------
@Composable
private fun PostCard(post: Post, onCategoryClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2458B6).copy(alpha = 0.65f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${post.userName} • ${post.timeAgo}", color = Color(0xFFEFF3FF),
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { onCategoryClick(post.category) }) {
                Text(post.category, color = Color(0xFFEAD6FF))
            }
            Spacer(Modifier.height(12.dp))
            Text(post.content, color = Color.White)

            post.imageUri?.let { uri ->
                Spacer(Modifier.height(12.dp))
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

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

// ---------------------- CREATE POST DIALOG ----------------------
@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onPostCreated: (Post) -> Unit) {
    val storage = Firebase.storage
    val firestore = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()

    var text by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first { it != "Home" }) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = !isUploading,
                onClick = {
                    if (text.isNotBlank()) {
                        isUploading = true
                        coroutineScope.launch {
                            var imageUrl: String? = null
                            if (imageUri != null) {
                                val fileName = "images/${System.currentTimeMillis()}.jpg"
                                val ref = storage.reference.child(fileName)
                                ref.putFile(imageUri!!).await()
                                imageUrl = ref.downloadUrl.await().toString()
                            }

                            val newPost = Post(
                                id = System.currentTimeMillis().toString(),
                                userName = "You",
                                timeAgo = "Just now",
                                category = selectedCategory,
                                content = text,
                                likes = 0,
                                comments = 0,
                                imageUri = imageUri
                            )

                            firestore.collection("posts").add(
                                hashMapOf(
                                    "userName" to newPost.userName,
                                    "timeAgo" to newPost.timeAgo,
                                    "category" to newPost.category,
                                    "content" to newPost.content,
                                    "likes" to newPost.likes,
                                    "comments" to newPost.comments,
                                    "imageUrl" to imageUrl
                                )
                            )

                            onPostCreated(newPost)
                            isUploading = false
                            onDismiss()
                        }
                    }
                }
            ) {
                Text(if (isUploading) "Posting..." else "Post")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        title = { Text("Create Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("What's on your mind?") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenuBox(
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it }
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1))
                ) {
                    Text(if (imageUri == null) "Add Photo" else "Change Photo")
                }

                imageUri?.let { uri ->
                    Spacer(Modifier.height(8.dp))
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    )
}

@Composable
fun DropdownMenuBox(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) { Text(selected) }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.filter { it != "Home" }.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onSelect(it)
                    expanded = false
                })
            }
        }
    }
}
