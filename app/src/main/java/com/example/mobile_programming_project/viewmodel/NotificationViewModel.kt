package com.example.mobile_programming_project.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobile_programming_project.data.Notifications
import com.example.mobile_programming_project.data.NotificationType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    // Observable notifications list
    val notifications = mutableStateListOf<Notifications>()

    // Unread count
    val unreadCount = mutableStateOf(0)

    // Current toast to display
    val currentToast = mutableStateOf<Notifications?>(null)

    init {
        loadNotifications()
        listenForNewNotifications()
    }

    /**
     * Load all notifications for the current user
     */
    fun loadNotifications() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val snapshot = firestore.collection("notifications")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val loadedNotifications = snapshot.documents.mapNotNull { doc ->
                    try {
                        Notifications(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            type = NotificationType.valueOf(
                                doc.getString("type") ?: "GENERAL"
                            ),
                            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                            isRead = doc.getBoolean("isRead") ?: false,
                            relatedPostId = doc.getString("relatedPostId"),
                            senderName = doc.getString("senderName")
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                notifications.clear()
                notifications.addAll(loadedNotifications)
                updateUnreadCount()
            } catch (e: Exception) {
                // Handle error silently or show error state
            }
        }
    }

    /**
     * Listen for new notifications in real-time
     */
    private fun listenForNewNotifications() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                for (change in snapshot.documentChanges) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.ADDED -> {
                            try {
                                val notification = Notifications(
                                    id = change.document.id,
                                    title = change.document.getString("title") ?: "",
                                    message = change.document.getString("message") ?: "",
                                    type = NotificationType.valueOf(
                                        change.document.getString("type") ?: "GENERAL"
                                    ),
                                    timestamp = change.document.getLong("timestamp")
                                        ?: System.currentTimeMillis(),
                                    isRead = change.document.getBoolean("isRead") ?: false,
                                    relatedPostId = change.document.getString("relatedPostId"),
                                    senderName = change.document.getString("senderName")
                                )

                                // Only add if not already in list
                                if (notifications.none { it.id == notification.id }) {
                                    notifications.add(0, notification)
                                    currentToast.value = notification
                                    updateUnreadCount()
                                }
                            } catch (e: Exception) {
                                // Skip malformed notification
                            }
                        }
                        else -> { /* Handle MODIFIED and REMOVED if needed */ }
                    }
                }
            }
    }

    /**
     * Mark a specific notification as read
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("notifications")
                    .document(notificationId)
                    .update("isRead", true)
                    .await()

                // Update local state
                val index = notifications.indexOfFirst { it.id == notificationId }
                if (index != -1) {
                    notifications[index] = notifications[index].copy(isRead = true)
                    updateUnreadCount()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch

                val unreadNotifications = notifications.filter { !it.isRead }

                unreadNotifications.forEach { notification ->
                    firestore.collection("notifications")
                        .document(notification.id)
                        .update("isRead", true)
                }

                // Update local state
                notifications.forEachIndexed { index, notification ->
                    if (!notification.isRead) {
                        notifications[index] = notification.copy(isRead = true)
                    }
                }
                updateUnreadCount()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Delete a notification
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("notifications")
                    .document(notificationId)
                    .delete()
                    .await()

                // Update local state
                notifications.removeIf { it.id == notificationId }
                updateUnreadCount()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Create a new notification (for testing or system notifications)
     */
    fun createNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.GENERAL,
        relatedPostId: String? = null,
        senderName: String? = null,
        targetUserId: String? = null
    ) {
        viewModelScope.launch {
            try {
                val userId = targetUserId ?: auth.currentUser?.uid ?: return@launch

                firestore.collection("notifications").add(
                    hashMapOf(
                        "userId" to userId,
                        "title" to title,
                        "message" to message,
                        "type" to type.name,
                        "timestamp" to System.currentTimeMillis(),
                        "isRead" to false,
                        "relatedPostId" to relatedPostId,
                        "senderName" to senderName
                    )
                ).await()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * Dismiss the current toast notification
     */
    fun dismissToast() {
        currentToast.value = null
    }

    private fun updateUnreadCount() {
        unreadCount.value = notifications.count { !it.isRead }
    }
}