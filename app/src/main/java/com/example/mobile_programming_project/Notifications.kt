package com.example.mobile_programming_project.data

data class Notifications(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.GENERAL,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedPostId: String? = null,
    val senderName: String? = null
)

enum class NotificationType {
    MARKETPLACE,      // Someone interested in your item
    LOST_AND_FOUND,   // Someone found your item or claimed yours
    SAFETY,           // Safety alert or incident update
    MESSAGE,          // New message
    GENERAL           // General app notifications
}