@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.mobile_programming_project.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mobile_programming_project.supabase
import com.example.mobile_programming_project.ui.Post
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

private const val SUPABASE_BUCKET = "user-uploads"

@Composable
fun LostAndFoundPostDialog(
    onDismiss: () -> Unit,
    onPostSubmitted: (Post) -> Unit
) {
    val firestore = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Form state
    var postType by remember { mutableStateOf("Lost") }
    var title by remember { mutableStateOf("") }
    var itemCategory by remember { mutableStateOf("Select Category") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dropdown states
    var postTypeExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val postTypes = listOf("Lost", "Found")
    val itemCategories = listOf(
        "Books & Textbooks",
        "Electronics & Tech",
        "ID & Cards",
        "Clothing & Accessories",
        "Bags & Backpacks",
        "Sports Equipment",
        "Personal Items",
        "Other"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) attachmentUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f),
        confirmButton = {
            Button(
                enabled = !isUploading &&
                        title.isNotBlank() &&
                        itemCategory != "Select Category" &&
                        location.isNotBlank() &&
                        description.isNotBlank(),
                onClick = {
                    isUploading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            // Upload attachment if present
                            var attachmentUrl: String? = null
                            if (attachmentUri != null) {
                                val bytes = context.contentResolver.openInputStream(attachmentUri!!)?.use {
                                    it.readBytes()
                                }
                                if (bytes != null) {
                                    val path = "lost-found/${System.currentTimeMillis()}.jpg"
                                    supabase.storage.from(SUPABASE_BUCKET).upload(
                                        path = path,
                                        data = bytes,
                                        upsert = true
                                    )
                                    attachmentUrl = supabase.storage.from(SUPABASE_BUCKET).publicUrl(path)
                                }
                            }

                            // Format date
                            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            val formattedDate = dateFormatter.format(selectedDate)

                            // Create post content with lost & found details
                            val postContent = buildString {
                                appendLine("${postType.uppercase()}: $title")
                                appendLine("Category: $itemCategory")
                                appendLine("Location: $location")
                                appendLine("Date: $formattedDate")
                                appendLine()
                                appendLine(description)
                            }

                            val newPost = Post(
                                id = System.currentTimeMillis().toString(),
                                userName = "You",
                                timeAgo = "Just now",
                                category = "Lost & Found",
                                content = postContent,
                                likes = 0,
                                comments = 0,
                                imageUrl = attachmentUrl
                            )

                            // Save to Firestore
                            firestore.collection("posts")
                                .document(newPost.id)   // <-- uses your custom ID
                                .set(
                                    hashMapOf(
                                        "id" to newPost.id,
                                        "userName" to newPost.userName,
                                        "timeAgo" to newPost.timeAgo,
                                        "category" to newPost.category,
                                        "content" to newPost.content,
                                        "likes" to newPost.likes,
                                        "comments" to newPost.comments,
                                        "imageUrl" to attachmentUrl,
                                        "postType" to postType,
                                        "title" to title,
                                        "itemCategory" to itemCategory,
                                        "location" to location,
                                        "date" to formattedDate
                                    )


                            ).await()

                            onPostSubmitted(newPost)
                            isUploading = false
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = "Failed to submit post: ${e.message}"
                            isUploading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (postType == "Lost") Color(0xFFFF9800) else Color(0xFF4CAF50)
                )
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
        title = {
            Text(
                "Lost & Found",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Post Type (Lost/Found)
                Text("Type", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(
                        onClick = { postTypeExpanded = true },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (postType == "Lost") Color(0xFFFF9800) else Color(0xFF4CAF50)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(postType)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = postTypeExpanded,
                        onDismissRequest = { postTypeExpanded = false }
                    ) {
                        postTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    postType = type
                                    postTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Divider()

                // Title
                Text("Title *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g., Black iPhone 16") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
                    singleLine = true
                )

                // Item Category
                Text("Item Category *", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(
                        onClick = { categoryExpanded = true },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (itemCategory == "Select Category")
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(itemCategory)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        itemCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    itemCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date
                Text("Date ${if (postType == "Lost") "Lost" else "Found"}", fontWeight = FontWeight.SemiBold)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate))
                }

                // Location
                Text("Location ${if (postType == "Lost") "Lost" else "Found"} *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("e.g., Library Building, Student Center") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
                    singleLine = true
                )

                // Description
                Text("Description *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Provide details to help identify the item...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = !isUploading,
                    maxLines = 6
                )

                // Attachment
                Text("Photo (Optional)", fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (attachmentUri == null) "📷 Add Photo" else "📷 Change Photo")
                }

                attachmentUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Attachment preview",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Error message
                errorMessage?.let { msg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            msg,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    )

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Date(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}