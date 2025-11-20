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

data class SafetyReport(
    val reportType: String, // "Anonymous" or "Identified"
    val incidentType: String,
    val dateTime: String,
    val location: String,
    val description: String,
    val attachmentUrl: String? = null
)

@Composable
fun SafetyReportDialog(
    onDismiss: () -> Unit,
    onReportSubmitted: (Post) -> Unit
) {
    val firestore = Firebase.firestore
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Form state
    var reportType by remember { mutableStateOf("Anonymous") }
    var incidentType by remember { mutableStateOf("Select Incident Type") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var attachmentUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dropdown states
    var reportTypeExpanded by remember { mutableStateOf(false) }
    var incidentTypeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val reportTypes = listOf("Anonymous", "Identified")
    val incidentTypes = listOf(
        "Theft",
        "Suspicious Activity",
        "Harassment",
        "Medical Emergency",
        "Fire/Hazard",
        "Wildlife Encounter",
        "Vehicle Incident",
        "Lost & Found",
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
                        incidentType != "Select Incident Type" &&
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
                                    val path = "safety-reports/${System.currentTimeMillis()}.jpg"
                                    supabase.storage.from(SUPABASE_BUCKET).upload(
                                        path = path,
                                        data = bytes,
                                        upsert = true
                                    )
                                    attachmentUrl = supabase.storage.from(SUPABASE_BUCKET).publicUrl(path)
                                }
                            }

                            // Format date and time
                            val dateFormatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                            val formattedDateTime = dateFormatter.format(selectedDate)

                            // Create post content with safety report details
                            val postContent = buildString {
                                appendLine("SAFETY REPORT")
                                appendLine("Incident: $incidentType")
                                appendLine("Location: $location")
                                appendLine("Date/Time: $formattedDateTime")
                                appendLine()
                                appendLine(description)
                            }

                            val userName = if (reportType == "Anonymous") "Anonymous User" else "You"

                            val newPost = Post(
                                id = System.currentTimeMillis().toString(),
                                userName = userName,
                                timeAgo = "Just now",
                                category = "Safety",
                                content = postContent,
                                likes = 0,
                                comments = 0,
                                imageUrl = attachmentUrl
                            )

                            // Save to Firestore
                            firestore.collection("posts").add(
                                hashMapOf(
                                    "id" to newPost.id,
                                    "userName" to newPost.userName,
                                    "timeAgo" to newPost.timeAgo,
                                    "category" to newPost.category,
                                    "content" to newPost.content,
                                    "likes" to newPost.likes,
                                    "comments" to newPost.comments,
                                    "imageUrl" to attachmentUrl,
                                    "reportType" to reportType,
                                    "incidentType" to incidentType,
                                    "location" to location,
                                    "dateTime" to formattedDateTime
                                )
                            ).await()

                            onReportSubmitted(newPost)
                            isUploading = false
                            onDismiss()
                        } catch (e: Exception) {
                            errorMessage = "Failed to submit report: ${e.message}"
                            isUploading = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F) // Red for safety/emergency
                )
            ) {
                Text(if (isUploading) "Submitting..." else "Submit Report")
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
                "Safety Report",
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
                // Report Type (Anonymous/Identified)
                Text("Report Type", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(
                        onClick = { reportTypeExpanded = true },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(reportType)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = reportTypeExpanded,
                        onDismissRequest = { reportTypeExpanded = false }
                    ) {
                        reportTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    reportType = type
                                    reportTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                Divider()

                // Incident Type
                Text("Incident Type *", fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(
                        onClick = { incidentTypeExpanded = true },
                        enabled = !isUploading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (incidentType == "Select Incident Type")
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(incidentType)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = incidentTypeExpanded,
                        onDismissRequest = { incidentTypeExpanded = false }
                    ) {
                        incidentTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    incidentType = type
                                    incidentTypeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date & Time
                Text("Date & Time", fontWeight = FontWeight.SemiBold)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(selectedDate))
                }

                // Location
                Text("Location *", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    placeholder = { Text("e.g., Library Building, Parking Lot C") },
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
                    placeholder = { Text("Describe what happened...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    enabled = !isUploading,
                    maxLines = 6
                )

                // Attachment
                Text("Attachment (Optional)", fontWeight = FontWeight.SemiBold)
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (attachmentUri == null) "📎 Add Photo/Evidence" else "📎 Change Attachment")
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