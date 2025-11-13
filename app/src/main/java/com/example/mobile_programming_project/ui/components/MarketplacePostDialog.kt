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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
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

private const val SUPABASE_BUCKET = "user-uploads"

data class MarketplaceItem(
    val itemName: String,
    val category: String,
    val condition: String,
    val price: String,
    val description: String,
    val imageUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplacePostDialog(
    onDismiss: () -> Unit,
    onPostSubmitted: (Post) -> Unit
) {
    val firestore = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Form state
    var itemName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Select Category") }
    var selectedCondition by remember { mutableStateOf("Select Condition") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dropdown states
    var categoryExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }

    // Categories and conditions
    val itemCategories = listOf(
        "Books & Study Materials",
        "Electronics & Tech",
        "Dorm Furniture",
        "School Supplies",
        "Clothing & Accessories",
        "Sports Equipment",
        "Other"
    )

    val itemConditions = listOf(
        "New",
        "Like New",
        "Used (Good)",
        "Used (Fair)"
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) imageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f),
        confirmButton = {
            Button(
                enabled = !isUploading &&
                        itemName.isNotBlank() &&
                        selectedCategory != "Select Category" &&
                        selectedCondition != "Select Condition" &&
                        price.isNotBlank() &&
                        description.isNotBlank(),
                onClick = {
                    isUploading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            // Upload image if present
                            var imageUrl: String? = null
                            if (imageUri != null) {
                                val bytes = context.contentResolver.openInputStream(imageUri!!)?.use {
                                    it.readBytes()
                                }
                                if (bytes != null) {
                                    val path = "marketplace/${System.currentTimeMillis()}.jpg"
                                    supabase.storage.from(SUPABASE_BUCKET).upload(
                                        path = path,
                                        data = bytes,
                                        upsert = true
                                    )
                                    imageUrl = supabase.storage.from(SUPABASE_BUCKET).publicUrl(path)
                                }
                            }

                            // Create post content with marketplace details
                            val postContent = buildString {
                                appendLine("MARKETPLACE LISTING")
                                appendLine("Item: $itemName")
                                appendLine("Category: $selectedCategory")
                                appendLine("Condition: $selectedCondition")
                                appendLine("Price: $$price")
                                appendLine()
                                appendLine(description)
                            }

                            val newPost = Post(
                                id = System.currentTimeMillis().toString(),
                                userName = "You",
                                timeAgo = "Just now",
                                category = "Marketplace",
                                content = postContent,
                                likes = 0,
                                comments = 0,
                                imageUrl = imageUrl
                            )

                            // Save to Firestore
                            firestore.collection("posts").add(
                                hashMapOf(
                                    "userName" to newPost.userName,
                                    "timeAgo" to newPost.timeAgo,
                                    "category" to newPost.category,
                                    "content" to newPost.content,
                                    "likes" to newPost.likes,
                                    "comments" to newPost.comments,
                                    "imageUrl" to imageUrl,
                                    "itemName" to itemName,
                                    "itemCategory" to selectedCategory,
                                    "itemCondition" to selectedCondition,
                                    "price" to price,
                                    "description" to description,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            ).await()

                            onPostSubmitted(newPost)
                            isUploading = false
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = "Failed to create listing: ${e.message}"
                            isUploading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5E35B1) // Purple to match your theme
                )
            ) {
                Text(if (isUploading) "Posting..." else "Post Listing")
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
                "Create Marketplace Listing",
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
                // Item Name
                Text("Item Name *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    placeholder = { Text("e.g., Calculus Textbook 3rd Edition") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
                    singleLine = true
                )

                Divider()

                // Category
                Text("Category *", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(
                        onClick = { categoryExpanded = true },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (selectedCategory == "Select Category")
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Category, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(selectedCategory)
                            }
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
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Condition
                Text("Condition *", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(
                        onClick = { conditionExpanded = true },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (selectedCondition == "Select Condition")
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCondition)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = conditionExpanded,
                        onDismissRequest = { conditionExpanded = false }
                    ) {
                        itemConditions.forEach { condition ->
                            DropdownMenuItem(
                                text = { Text(condition) },
                                onClick = {
                                    selectedCondition = condition
                                    conditionExpanded = false
                                }
                            )
                        }
                    }
                }

                // Price
                Text("Price *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        // Only allow numbers and decimal point
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                            price = it
                        }
                    },
                    placeholder = { Text("e.g., 25.00") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                    },
                    prefix = { Text("$") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
                    singleLine = true
                )

                // Description
                Text("Description *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Describe the item, why you're selling, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = !isUploading,
                    maxLines = 6
                )

                // Image Upload
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
                    Text(if (imageUri == null) "Add Photo" else "Change Photo")
                }

                imageUri?.let { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Item preview",
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
}