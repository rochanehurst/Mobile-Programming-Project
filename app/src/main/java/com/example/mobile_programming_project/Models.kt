// ===== DATA MODELS =====
package com.example.mobile_programming_project.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Comment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)

data class Like(
    val id: String = "",
    val postId: String = "",
    val userId: String = ""
)

