// ===== UI COMPONENTS FILE =====


package com.example.mobile_programming_project.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobile_programming_project.FirestoreManager
import com.example.mobile_programming_project.data.Comment
import kotlinx.coroutines.launch

@Composable
fun CommentDialog(
    postId: String,
    onDismiss: () -> Unit,
    currentUserEmail: String?
) {
    val coroutineScope = rememberCoroutineScope()
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var newCommentText by remember { mutableStateOf("") }
    var isPosting by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        isLoading = true
        comments = FirestoreManager.getComments(postId)
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comments") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No comments yet", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(comments) { comment ->
                            CommentItem(
                                comment = comment,
                                currentUserEmail = currentUserEmail,
                                onDelete = {
                                    coroutineScope.launch {
                                        if (FirestoreManager.deleteComment(comment.id, postId)) {
                                            comments = comments.filter { it.id != comment.id }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp, max = 80.dp),
                    enabled = !isPosting,
                    singleLine = false,
                    textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize)
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            isPosting = true
                            coroutineScope.launch {
                                if (FirestoreManager.addComment(postId, newCommentText)) {
                                    newCommentText = ""
                                    comments = FirestoreManager.getComments(postId)
                                }
                                isPosting = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPosting && newCommentText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1))
                ) {
                    Text(if (isPosting) "Posting..." else "Post Comment")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun CommentItem(
    comment: Comment,
    currentUserEmail: String?,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.userName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
                if (currentUserEmail?.substringBefore("@") == comment.userName) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete comment",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun LikeButton(
    postId: String,
    initialLikeCount: Int,
    onLikeChange: (Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var likeCount by remember { mutableStateOf(initialLikeCount) }
    var isLiked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(postId) {
        isLiked = FirestoreManager.checkIfLiked(postId)
        isLoading = false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.wrapContentSize()
    ) {
        IconButton(
            onClick = {
                coroutineScope.launch {
                    if (isLiked) {
                        if (FirestoreManager.removeLike(postId)) {
                            isLiked = false
                            likeCount--
                            onLikeChange(likeCount)
                        }
                    } else {
                        if (FirestoreManager.addLike(postId)) {
                            isLiked = true
                            likeCount++
                            onLikeChange(likeCount)
                        }
                    }
                }
            },
            enabled = !isLoading
        ) {
            Icon(
                if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (isLiked) Color.Red else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(likeCount.toString(), color = Color.White)
    }
}