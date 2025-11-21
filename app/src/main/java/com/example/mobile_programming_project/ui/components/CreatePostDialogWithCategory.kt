package com.example.mobile_programming_project.ui.components

import androidx.compose.runtime.Composable
import com.example.mobile_programming_project.ui.Post
import com.example.mobile_programming_project.ui.components.CreatePostDialogWithCategory as InternalCreatePostDialogWithCategory

/**
 * Thin wrapper so HomeScreen can reference this dialog from the ui.components package.
 * The real implementation is in ui/HomeScreen.kt.
 */
@Composable
fun CreatePostDialogWithCategory(
    category: String,
    onDismiss: () -> Unit,
    onPostCreated: (Post) -> Unit
) {
    InternalCreatePostDialogWithCategory(
        category = category,
        onDismiss = onDismiss,
        onPostCreated = onPostCreated
    )
}
