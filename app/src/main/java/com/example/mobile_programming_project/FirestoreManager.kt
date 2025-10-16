
// ===== FIRESTORE MANAGER =====


package com.example.mobile_programming_project

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.example.mobile_programming_project.data.Comment
import kotlinx.coroutines.tasks.await

object FirestoreManager {
    private val firestore = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    // ===== LIKES =====
    suspend fun addLike(postId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val likeId = "$postId-$userId"

            firestore.collection("likes").document(likeId).set(
                mapOf(
                    "postId" to postId,
                    "userId" to userId
                )
            ).await()

            // Increment likes count on post
            firestore.collection("posts").document(postId)
                .update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun removeLike(postId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val likeId = "$postId-$userId"

            firestore.collection("likes").document(likeId).delete().await()

            // Decrement likes count on post
            firestore.collection("posts").document(postId)
                .update("likes", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun checkIfLiked(postId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val likeId = "$postId-$userId"

            val doc = firestore.collection("likes").document(likeId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // ===== COMMENTS =====
    suspend fun addComment(postId: String, content: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val userName = auth.currentUser?.email?.substringBefore("@") ?: "Unknown"

            firestore.collection("comments").add(
                mapOf(
                    "postId" to postId,
                    "userId" to userId,
                    "userName" to userName,
                    "content" to content,
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
            ).await()

            // Increment comments count on post
            firestore.collection("posts").document(postId)
                .update("comments", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getComments(postId: String): List<Comment> {
        return try {
            val snapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Comment(
                        id = doc.id,
                        postId = doc.getString("postId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Unknown",
                        content = doc.getString("content") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun deleteComment(commentId: String, postId: String): Boolean {
        return try {
            firestore.collection("comments").document(commentId).delete().await()

            // Decrement comments count on post
            firestore.collection("posts").document(postId)
                .update("comments", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }
}