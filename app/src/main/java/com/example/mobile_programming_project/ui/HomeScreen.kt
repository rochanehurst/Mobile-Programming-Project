@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.mobile_programming_project.ui

import io.github.jan.supabase.storage.storage
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.mobile_programming_project.supabase
import com.example.mobile_programming_project.ui.components.NotificationBadge
import com.example.mobile_programming_project.ui.components.NotificationToast
import com.example.mobile_programming_project.ui.components.CommentDialog
import com.example.mobile_programming_project.ui.components.LikeButton
import com.example.mobile_programming_project.viewmodel.NotificationViewModel

private const val SUPABASE_BUCKET = "user-uploads"

data class Post(
    val id: String,
    val userName: String,
    val timeAgo: String,
    val category: String,
    val content: String,
    var likes: Int,
    var comments: Int,
    val imageUrl: String? = null
)

val categories = listOf("Home", "Lost & Found", "Marketplace", "Safety")

val demoPost = listOf(
    Post(
        id = "demo1",
        userName = "Edein Vindain",
        timeAgo = "5 minutes",
        category = "Lost & Found",
        content = "Lost my water bottle. It looks like this.",
        likes = 999,
        comments = 320,
        imageUrl = "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=500"
    ),
    Post(
        id = "demo2",
        userName = "Dian Cinne",
        timeAgo = "10 minutes",
        category = "Marketplace",
        content = "Selling a used backpack.",
        likes = 10,
        comments = 300,
        imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500"
    ),
    Post(
        id = "demo3",
        userName = "Eat meat",
        timeAgo = "23 minutes",
        category = "Marketplace",
        content = "I have an air mattress for sale. 27 dollars.",
        likes = 5,
        comments = 12,
        imageUrl = "https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=500"
    ),
    Post(
        id = "demo4",
        userName = "John Put",
        timeAgo = "23 minutes",
        category = "Safety",
        content = "Someone stole my backpack. I think they're trying to sell in the marketplace",
        likes = 45,
        comments = 89,
        imageUrl = null
    ),
    Post(
        id = "demo5",
        userName = "Dian Cinne",
        timeAgo = "39 minutes",
        category = "Safety",
        content = "I was walking to the parking lot and saw a huge coyote. Be careful out there everyone.",
        likes = 10,
        comments = 300,
        imageUrl = null
    )
)

@Composable
fun HomeScreen(
    onSignOut: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    viewModel: NotificationViewModel = viewModel()
) {
    val gradient = Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5)))
    var selectedCategory by remember { mutableStateOf("Home") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var showCreatePost by remember { mutableStateOf(false) }
    val posts = remember { mutableStateListOf<Post>() }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email

    LaunchedEffect(Unit) {
        try {
            val snapshot = Firebase.firestore.collection("posts").get().await()
            val loaded = snapshot.documents.mapNotNull { doc ->
                try {
                    Post(
                        id = doc.id,
                        userName = doc.getString("userName") ?: "Unknown",
                        timeAgo = doc.getString("timeAgo") ?: "",
                        category = doc.getString("category") ?: "Home",
                        content = doc.getString("content") ?: "",
                        likes = (doc.getLong("likes") ?: 0).toInt(),
                        comments = (doc.getLong("comments") ?: 0).toInt(),
                        imageUrl = doc.getString("imageUrl")
                    )
                } catch (e: Exception) {
                    null
                }
            }
            posts.clear()
            if (loaded.isEmpty()) {
                posts.addAll(demoPost)
            } else {
                posts.addAll(loaded)
            }
        } catch (e: Exception) {
            posts.addAll(demoPost)
        }
        isLoading = false
    }

    val filteredPosts = if (selectedCategory == "Home") posts
    else posts.filter { it.category.equals(selectedCategory, ignoreCase = true) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        Box {
                            IconButton(onClick = onNavigateToNotifications) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                            if (viewModel.unreadCount.value > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 8.dp, end = 8.dp)
                                ) {
                                    NotificationBadge(count = viewModel.unreadCount.value)
                                }
                            }
                        }

                        TextButton(onClick = onSignOut) {
                            Text("Sign out", color = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredPosts) { post ->
                            PostCard(
                                post = post,
                                currentUserEmail = currentUserEmail,
                                onCategoryClick = { clickedCategory ->
                                    selectedCategory = clickedCategory
                                },
                                onLikeChange = { newLikeCount ->
                                    post.likes = newLikeCount
                                }
                            )
                        }
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(top = 70.dp)
        ) {
            NotificationToast(
                notification = viewModel.currentToast.value,
                onDismiss = { viewModel.dismissToast() }
            )
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    currentUserEmail: String?,
    onCategoryClick: (String) -> Unit,
    onLikeChange: (Int) -> Unit
) {
    var showCommentDialog by remember { mutableStateOf(false) }

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

            post.imageUrl?.let { url ->
                Spacer(Modifier.height(12.dp))
                AsyncImage(
                    model = url,
                    contentDescription = "Post image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(12.dp))
            RowActions(
                post = post,
                onCommentClick = { showCommentDialog = true },
                onLikeChange = onLikeChange
            )
        }
    }

    if (showCommentDialog) {
        CommentDialog(
            postId = post.id,
            onDismiss = { showCommentDialog = false },
            currentUserEmail = currentUserEmail
        )
    }
}

@Composable
private fun RowActions(
    post: Post,
    onCommentClick: () -> Unit,
    onLikeChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LikeButton(
            postId = post.id,
            initialLikeCount = post.likes,
            onLikeChange = onLikeChange
        )

        Spacer(Modifier.weight(1f))

        IconButton(onClick = onCommentClick) {
            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments", tint = Color.White)
        }
        Text("${post.comments}", color = Color.White)

        Spacer(Modifier.weight(1f))

        IconButton(onClick = { }) {
            Icon(Icons.Outlined.Send, contentDescription = "Share", tint = Color.White)
        }
    }
}

@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onPostCreated: (Post) -> Unit) {
    val firestore = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var text by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first { it != "Home" }) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = !isUploading && text.isNotBlank(),
                onClick = {
                    if (text.isNotBlank()) {
                        isUploading = true
                        errorMessage = null
                        coroutineScope.launch {
                            try {
                                var imageUrl: String? = null
                                if (imageUri != null) {
                                    val bytes = context.contentResolver.openInputStream(imageUri!!)?.use {
                                        it.readBytes()
                                    }
                                    if (bytes != null) {
                                        val path = "images/${System.currentTimeMillis()}.jpg"
                                        supabase.storage.from(SUPABASE_BUCKET).upload(
                                            path = path,
                                            data = bytes,
                                            upsert = true
                                        )
                                        imageUrl = supabase.storage.from(SUPABASE_BUCKET).publicUrl(path)
                                    }
                                }

                                val newPost = Post(
                                    id = System.currentTimeMillis().toString(),
                                    userName = "You",
                                    timeAgo = "Just now",
                                    category = selectedCategory,
                                    content = text,
                                    likes = 0,
                                    comments = 0,
                                    imageUrl = imageUrl
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
                                ).await()

                                onPostCreated(newPost)
                                isUploading = false
                                onDismiss()
                            } catch (e: Exception) {
                                errorMessage = "Failed to create post: ${e.message}"
                                isUploading = false
                            }
                        }
                    }
                }
            ) {
                Text(if (isUploading) "Posting..." else "Post")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Cancel")
            }
        },
        title = { Text("Create Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("What's on your mind?") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )

                DropdownMenuBox(
                    selected = selectedCategory,
                    onSelect = { selectedCategory = it },
                    enabled = !isUploading
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1)),
                    enabled = !isUploading
                ) {
                    Text(if (imageUri == null) "Add Photo" else "Change Photo")
                }

                imageUri?.let { uri ->
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                errorMessage?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(msg, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
fun DropdownMenuBox(selected: String, onSelect: (String) -> Unit, enabled: Boolean = true) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled
        ) {
            Text(selected)
        }
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