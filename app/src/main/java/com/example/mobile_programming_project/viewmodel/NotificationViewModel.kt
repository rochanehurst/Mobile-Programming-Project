package com.example.mobile_programming_project.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.mobile_programming_project.data.Notifications
import com.example.mobile_programming_project.data.NotificationType

class NotificationViewModel : ViewModel() {

    // List of all notifications
    val notifications = mutableStateListOf<Notifications>()

    // Currently showing toast notification
    val currentToast = mutableStateOf<Notifications?>(null)

    // Unread count
    val unreadCount = mutableStateOf(0)

    init {
        // TODO: REPLACE THIS WITH FIREBASE LISTENER
        loadMockNotifications()
    }

    // Mock data - your partner will replace with Firebase queries
    private fun loadMockNotifications() {
        notifications.addAll(listOf(
            Notifications(
                id = "1",
                title = "Item Inquiry",
                message = "Sarah Chen is interested in your textbook",
                type = NotificationType.MARKETPLACE,
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = false,
                senderName = "Sarah Chen"
            ),
            Notifications(
                id = "2",
                title = "Item Found!",
                message = "Your lost wallet may have been found",
                type = NotificationType.LOST_AND_FOUND,
                timestamp = System.currentTimeMillis() - 7200000,
                isRead = false
            ),
            Notifications(
                id = "3",
                title = "Safety Alert",
                message = "Incident reported near Library - stay alert",
                type = NotificationType.SAFETY,
                timestamp = System.currentTimeMillis() - 10800000,
                isRead = true
            )
        ))
        updateUnreadCount()
    }

    // Mark notification as read
    fun markAsRead(notificationId: String) {
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
            updateUnreadCount()
            // TODO: ADD FIREBASE UPDATES HERE
        }
    }

    // Mark all as read
    fun markAllAsRead() {
        for (i in notifications.indices) {
            notifications[i] = notifications[i].copy(isRead = true)
        }
        updateUnreadCount()
        // TODO: ADD FIREBASE BATCH UPDATE HERE
    }

    // Delete notification
    fun deleteNotification(notificationId: String) {
        notifications.removeIf { it.id == notificationId }
        updateUnreadCount()
        // TODO: ADD FIREBASE DELETE HERE
    }

    // Show toast notification
    fun showToast(notification: Notifications) {
        currentToast.value = notification
    }

    // Dismiss toast
    fun dismissToast() {
        currentToast.value = null
    }

    // Update unread count
    private fun updateUnreadCount() {
        unreadCount.value = notifications.count { !it.isRead }
    }

    // Add new notification (called when Firebase sends new one)
    fun addNotification(notification: Notifications) {
        notifications.add(0, notification)
        updateUnreadCount()
        showToast(notification)
    }
}